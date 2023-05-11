package gamein2.schedule.service;

import gamein2.schedule.model.dto.RegionDTO;
import gamein2.schedule.model.dto.TimeResultDTO;
import gamein2.schedule.model.entity.*;
import gamein2.schedule.model.enums.LogType;
import gamein2.schedule.model.enums.ShippingMethod;
import gamein2.schedule.model.repository.*;
import gamein2.schedule.util.GameinTradeTasks;
import gamein2.schedule.util.RestUtil;
import gamein2.schedule.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@Configuration
public class ScheduleService {
    private final TimeRepository timeRepository;
    private final TeamRepository teamRepository;
    private final TeamResearchRepository teamResearchRepository;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;
    private final ProductRepository productRepository;
    private final DemandRepository demandRepository;
    private final RegionRepository regionRepository;
    private final LogRepository logRepository;
    private final StorageProductRepository storageProductRepository;
    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final DemandLogRepository demandLogRepository;
    private final BuildingRepository buildingRepository;
    private final BuildingInfoRepository buildingInfoRepository;
    private final WealthLogRepository wealthLogRepository;

    @Value("${live.data.url}")
    private String liveUrl;

    public ScheduleService(TimeRepository timeRepository, TeamRepository teamRepository, TeamResearchRepository teamResearchRepository, FinalProductSellOrderRepository finalProductSellOrderRepository, ProductRepository productRepository, DemandRepository demandRepository, RegionRepository regionRepository, LogRepository logRepository, StorageProductRepository storageProductRepository, OrderRepository orderRepository, OfferRepository offerRepository, DemandLogRepository demandLogRepository, BuildingRepository buildingRepository, BuildingInfoRepository buildingInfoRepository, WealthLogRepository wealthLogRepository) {
        this.timeRepository = timeRepository;
        this.teamRepository = teamRepository;
        this.teamResearchRepository = teamResearchRepository;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.productRepository = productRepository;
        this.demandRepository = demandRepository;
        this.regionRepository = regionRepository;
        this.logRepository = logRepository;
        this.storageProductRepository = storageProductRepository;
        this.orderRepository = orderRepository;
        this.offerRepository = offerRepository;
        this.demandLogRepository = demandLogRepository;
        this.buildingRepository = buildingRepository;
        this.buildingInfoRepository = buildingInfoRepository;
        this.wealthLogRepository = wealthLogRepository;
    }

    @Transactional
    @Scheduled(fixedDelay = 240, timeUnit = TimeUnit.SECONDS)
    public void storageCost() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        if (time.getIsRegionPayed()) {
            List<Team> allTeams = teamRepository.findAll();
            TimeResultDTO timeResultDTO = TimeUtil.getTime(time);
            for (Team team : allTeams) {
                if (team.getId().equals(0L)) continue;
                long cost = 0L;
                List<StorageProduct> teamProducts = storageProductRepository.findAllByTeamId(team.getId());
                for (StorageProduct storageProduct : teamProducts) {
                    long totalVolume = storageProduct.getInStorageAmount();
                    cost += totalVolume * storageProduct.getProduct().getMinPrice();
                }
                if (team.getBalance() >= cost / time.getStorageCostScale()) {
                    team.setBalance(team.getBalance() - cost / time.getStorageCostScale());
                    Log log = new Log();
                    log.setType(LogType.STORAGE_COST);
                    log.setTeam(team);
                    log.setTotalCost(cost / time.getStorageCostScale());
                    log.setProductCount(0L);
                    log.setTimestamp(LocalDateTime.of(Math.toIntExact(timeResultDTO.getYear()),
                            Math.toIntExact(timeResultDTO.getMonth()),
                            Math.toIntExact(timeResultDTO.getDay()),
                            12,
                            23));
                    logRepository.save(log);

                }
            }
            String text = "هزینه انبارداری این ماه از حساب شما برداشت شد.";
            RestUtil.sendNotificationToAll(text, "UPDATE_BALANCE", liveUrl);
        }
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void buyFinalProducts() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        try {
            System.out.println("final product orders task now commencing:\n");
            LocalDateTime nextTime = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5);
            time.setNextFinalOrderTime(nextTime);
            timeRepository.save(time);

            long fiveMinutesFromBeginning =
                    (TimeUtil.getTime(time).getDurationMillis() / (5 * 60 * 1000)) * 5;
            List<FinalProductSellOrder> orders =
                    finalProductSellOrderRepository.findAllByClosedIsFalseAndCancelledIsFalse();
            List<Product> products = productRepository.findAllByLevelBetween(3, 3);
            TeamResearch first = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(11L, LocalDateTime.now(ZoneOffset.UTC));
            TeamResearch second = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(12L, LocalDateTime.now(ZoneOffset.UTC));
            TeamResearch third = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(13L, LocalDateTime.now(ZoneOffset.UTC));
            TeamResearch fourth = teamResearchRepository.findFirstBySubject_IdAndEndTimeIsBeforeOrderByEndTime(14L, LocalDateTime.now(ZoneOffset.UTC));
            Optional<Demand> demandOptional = demandRepository.findById(fiveMinutesFromBeginning);
            if (demandOptional.isEmpty()) {
                System.err.printf("Demand %d not found!\n", fiveMinutesFromBeginning);
                return;
            }
            Demand demand = demandOptional.get();

            new GameinTradeTasks(
                    logRepository, (int) (time.getDemandMultiplier() * demand.getDemand()),
                    first != null ? first.getEndTime() : null,
                    second != null ? second.getEndTime() : null,
                    third != null ? third.getEndTime() : null,
                    fourth != null ? fourth.getEndTime() : null,
                    products,
                    orders,
                    finalProductSellOrderRepository, storageProductRepository, timeRepository, demandLogRepository,
                    fiveMinutesFromBeginning).run();
            finalProductSellOrderRepository.saveAll(orders);
            teamRepository.saveAll(orders.stream().map(FinalProductSellOrder::getSubmitter).collect(Collectors.toList()));
        } catch (Exception e) {
            System.err.println("Error in scheduled task: trade service handler:");
            System.err.println(e.getMessage());
        }
    }

    /*@Scheduled(fixedDelay = 3, timeUnit = TimeUnit.MINUTES)
    public void tradeOffers() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        Optional<Team> teamOptional = teamRepository.findById(0L);
        if (teamOptional.isEmpty()) {
            System.err.println("gamein team not found!");
            return;
        }
        Team gamein = teamOptional.get();
        for (Order order : orderRepository.allConvincingOrders(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(6))) {
            System.out.println(order);
            Offer offer = new Offer();
            offer.setOfferer(gamein);
            offer.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));
            offer.setOrder(order);
            offer.setShippingMethod(ShippingMethod.SAME_REGION);
            offerRepository.save(offer);
        }
        gamein.setRegion(new Random().nextInt(8) + 1);
        teamRepository.save(gamein);
    }*/

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void saveTeamsWealth() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsGamePaused()) return;

        for (Team team : teamRepository.findAll()) {
            WealthLog log = new WealthLog();
            log.setTeam(team);
            log.setWealth(getTeamWealth(team, storageProductRepository, buildingRepository, buildingInfoRepository));
            log.setTime(LocalDateTime.now(ZoneOffset.UTC));
            log.setTenMinuteRound(TimeUtil.getTime(time).getDurationMillis() / (10 * 60 * 1000));
            wealthLogRepository.save(log);
        }
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void payRegionPrice() {
        Time time = timeRepository.findById(1L).get();
        if (time.getIsRegionPayed()) return;
        if (time.getIsGamePaused()) return;

        Long duration = Duration.between(time.getBeginTime(), LocalDateTime.now(ZoneOffset.UTC)).toSeconds();
        boolean isChooseRegionFinished = duration - time.getStoppedTimeSeconds() > time.getChooseRegionDuration();
        if (!time.getIsRegionPayed() && isChooseRegionFinished) {
            /*List<Region> regions = regionRepository.findAll();*/
            Map<Integer, Long> regionsPopulation = RegionDTO.getRegionsPopulation(teamRepository.getRegionsPopulation());
            for (int i = 1; i < 9 ; i ++){
                if (!regionsPopulation.containsKey(i))
                    regionsPopulation.put(i,0L);
            }

            List<Team> teams = teamRepository.findAll();
            for (Team team : teams) {
                if (team.getRegion() == 0) {
                    Random random = new Random();
                    team.setRegion(random.nextInt(8) + 1);
                    regionsPopulation.put(team.getRegion(), regionsPopulation.get(team.getRegion()) + 1);
                }
            }

            Map<Long, Long> regionsPrice = new HashMap<>();
            for (int i = 1; i < 9; i++) {
                Region region = regionRepository.findFirstByRegionId(i);
                Long price = calculateRegionPrice(regionsPopulation.get(i));
                regionsPrice.put((long) i, price);
                region.setRegionPayed(price);
                regionRepository.save(region);
            }
            for (Team team : teams) {
                team.setBalance(team.getBalance() - regionsPrice.get((long) team.getRegion()));
            }
            teamRepository.saveAll(teams);
            time.setIsRegionPayed(true);
            timeRepository.save(time);
            String text = "هزینه زمین از حساب شما برداشت شد.";
            RestUtil.sendNotificationToAll(text, "UPDATE_BALANCE", liveUrl);
        }
    }

    private Long getTeamWealth(Team team, StorageProductRepository storageProductRepository,
                               BuildingRepository buildingRepository, BuildingInfoRepository buildingInfoRepository) {
        long wealth = 0L;
        List<StorageProduct> teamsProduct = storageProductRepository.findAllByTeamId(team.getId());
        for (StorageProduct storageProduct : teamsProduct) {
            wealth += (long) storageProduct.getProduct().getPrice() * storageProduct.getInStorageAmount();
        }
        List<Building> teamBuildings = buildingRepository.findAllByTeamId(team.getId());
        for (Building building : teamBuildings) {
            wealth += buildingInfoRepository.findById(building.getType()).orElseGet(BuildingInfo::new).getBuildPrice();
        }
        wealth += team.getBalance();
        return wealth;
    }

    private Long calculateRegionPrice(Long currentPopulation) {
        Time time = timeRepository.findById(1L).get();
        Long scale = time.getScale();
        Integer teamsCount = teamRepository.getCount();
        return (long) ((1 + (2.25 / (0.8 + 9 * Math.exp(-0.8 * (16 * currentPopulation / (teamsCount - 0.26)))))) * scale);
    }
}
