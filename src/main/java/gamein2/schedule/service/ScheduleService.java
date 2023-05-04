package gamein2.schedule.service;

import gamein2.schedule.model.entinty.*;
import gamein2.schedule.model.repository.*;
import gamein2.schedule.util.GameinTradeTasks;
import gamein2.schedule.util.RestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class ScheduleService {
    private final TimeRepository timeRepository;
    private final TeamRepository teamRepository;

    private final TeamResearchRepository teamResearchRepository;

    private final FinalProductSellOrderRepository finalProductSellOrderRepository;

    private final ProductRepository productRepository;

    private final DemandRepository demandRepository;

    private final BrandRepository brandRepository;

    private final RegionRepository regionRepository;
    @Value("${live.data.url}")
    private String liveUrl;

    private final StorageProductRepository storageProductRepository;

    public ScheduleService(TimeRepository timeRepository, TeamRepository teamRepository, TeamResearchRepository teamResearchRepository, FinalProductSellOrderRepository finalProductSellOrderRepository, ProductRepository productRepository, DemandRepository demandRepository, BrandRepository brandRepository, RegionRepository regionRepository, StorageProductRepository storageProductRepository) {
        this.timeRepository = timeRepository;
        this.teamRepository = teamRepository;
        this.teamResearchRepository = teamResearchRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.productRepository = productRepository;
        this.demandRepository = demandRepository;
        this.brandRepository = brandRepository;
        this.regionRepository = regionRepository;
        this.storageProductRepository = storageProductRepository;
    }

    @Transactional
    @Scheduled(fixedDelay = 240, timeUnit = TimeUnit.SECONDS)
    public void storageCost() {
        Time time = timeRepository.findById(1L).get();
        if (!time.getIsGamePaused() && time.getIsRegionPayed()) {
            List<Team> allTeams = teamRepository.findAll();
            for (Team team : allTeams) {
                long cost = 0L;
                List<StorageProduct> teamProducts = storageProductRepository.findAllByTeamId(team.getId());
                for (StorageProduct storageProduct : teamProducts) {
                    long totalVolume = (long) storageProduct.getProduct().getUnitVolume() * storageProduct.getInStorageAmount();
                    cost += totalVolume * storageProduct.getProduct().getMinPrice();
                }

                if (team.getBalance() >= cost / time.getStorageCostScale())
                    team.setBalance(team.getBalance() - cost / time.getStorageCostScale());
            }
            String text = "هزینه انبارداری این ماه از حساب شما برداشت شد.";
            RestUtil.sendNotificationToAll(text, "UPDATE_BALANCE", liveUrl);
        }
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    private void buy() {
        try {
            System.out.println("scheduled task");
            Time time = timeRepository.findById(1L).get();
            long fiveMinutesFromBeginning =
                    (Duration.ofSeconds(
                            Duration.between(time.getBeginTime(), LocalDateTime.now(ZoneOffset.UTC)).toSeconds() - time.getStoppedTimeSeconds()
                    ).toMinutes() / 5) * 5;
            List<FinalProductSellOrder> orders =
                    finalProductSellOrderRepository.findAllByClosedIsFalseAndCancelledIsFalse();
            List<Product> products = productRepository.findAllByLevelBetween(3, 3);
            TeamResearch first = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(11L);
            TeamResearch second = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(12L);
            TeamResearch third = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(13L);
            TeamResearch fourth = teamResearchRepository.findFirstBySubject_IdOrderByEndTime(14L);
            Optional<Demand> demandOptional = demandRepository.findById(fiveMinutesFromBeginning);
            if (demandOptional.isEmpty()) {
                System.err.printf("Demand %d not found!\n", fiveMinutesFromBeginning);
                return;
            }
            Demand demand = demandOptional.get();

            List<Team> teams = teamRepository.findAll();
            List<Brand> previousBrands = brandRepository.findAllByPeriod(fiveMinutesFromBeginning - 1);
            List<Brand> previousPreviousBrands = brandRepository.findAllByPeriod(fiveMinutesFromBeginning - 2);

            HashMap<Long, Double> newBrandsMap = new GameinTradeTasks(
                    previousBrands, previousPreviousBrands, demand.getDemand(),
                    first != null ? first.getEndTime() : null,
                    second != null ? second.getEndTime() : null,
                    third != null ? third.getEndTime() : null,
                    fourth != null ? fourth.getEndTime() : null,
                    products,
                    orders,
                    teams,
                    finalProductSellOrderRepository, storageProductRepository).run();
            List<Brand> newBrands = new ArrayList<>();
            for (Map.Entry<Long, Double> brand : newBrandsMap.entrySet()) {
                Brand b = new Brand();
                b.setTeam(teamRepository.findById(brand.getKey()).get());
                b.setBrand(brand.getValue());
                b.setPeriod(fiveMinutesFromBeginning);
                newBrands.add(b);
            }
            brandRepository.saveAll(newBrands);
            finalProductSellOrderRepository.saveAll(orders);
            teamRepository.saveAll(orders.stream().map(FinalProductSellOrder::getSubmitter).collect(Collectors.toList()));
            Date nextTime = new Date(
                    (new Date().getTime()) + (5 * 60 * 1000)
            );
            time.setNextFinalOrderTime(nextTime);
            timeRepository.save(time);
        } catch (Exception e) {
            System.err.println("Error in scheduled task: trade service handler:");
            System.err.println(e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void payRegionPrice() {
        Time time = timeRepository.findById(1L).get();
        Long duration =  Duration.between(time.getBeginTime(),LocalDateTime.now(ZoneOffset.UTC)).toSeconds();
        boolean isChooseRegionFinished = duration - time.getStoppedTimeSeconds() > time.getChooseRegionDuration();
        if (! time.getIsRegionPayed() && isChooseRegionFinished){
            List<Region> regions = regionRepository.findAll();
            List<Team> teams = teamRepository.findAll();
            for (Team team : teams){
                if (team.getRegion() == 0){
                    Random random = new Random();
                    team.setRegion(random.nextInt(8) + 1);
                    Region region = regions.get(team.getRegion() - 1);
                    region.setRegionPopulation(region.getRegionPopulation() + 1);
                }
            }
            for (Region region: regions){
                region.setRegionPayed(calculateRegionPrice(region.getRegionPopulation()));
            }
            for (Team team : teams){
                team.setBalance(team.getBalance() - regions.get(team.getRegion() - 1).getRegionPayed());
            }
            regionRepository.saveAll(regions);
            teamRepository.saveAll(teams);
            time.setIsRegionPayed(true);
            timeRepository.save(time);
            String text = "هزینه زمین از حساب شما برداشت شد.";
            RestUtil.sendNotificationToAll(text,"UPDATE_BALANCE",liveUrl);
        }
    }

    private Long calculateRegionPrice(Long currentPopulation) {
        Time time = timeRepository.findById(1L).get();
        Long scale = time.getScale();
        Integer teamsCount = teamRepository.getCount();
        return (long) ((1 + (2.25 / (0.8 + 9 * Math.exp(-0.8 * (16 * currentPopulation / (teamsCount - 0.26)))))) * scale);
    }

}
