package gamein2.schedule.util;

import gamein2.schedule.model.dto.TimeResultDTO;
import gamein2.schedule.model.entity.*;
import gamein2.schedule.model.enums.LogType;
import gamein2.schedule.model.repository.*;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GameinTradeTasks {

    private final LogRepository logRepository;
    private final HashMap<Long, Integer> demands = new HashMap<>();
    private final int totalDemand;
    private final LocalDateTime firstTime;
    private final LocalDateTime secondTime;
    private final LocalDateTime thirdTime;
    private final LocalDateTime fourthTime;
    private final List<Product> products;
    private final List<FinalProductSellOrder> orders;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;
    private final StorageProductRepository spRepo;
    private final TimeRepository timeRepository;
    private final DemandLogRepository demandLogRepository;
    private final Long fiveMinutesFromBeginning;

    public GameinTradeTasks(LogRepository logRepository, int totalDemand,
                            LocalDateTime firstTime, LocalDateTime secondTime, LocalDateTime thirdTime, LocalDateTime fourthTime, List<Product> products,
                            List<FinalProductSellOrder> orders,
                            FinalProductSellOrderRepository finalProductSellOrderRepository,
                            StorageProductRepository spRepo, TimeRepository timeRepository,
                            DemandLogRepository demandLogRepository, Long fiveMinutesFromBeginning) {
        this.logRepository = logRepository;
        this.totalDemand = totalDemand;
        this.firstTime = firstTime;
        this.secondTime = secondTime;
        this.thirdTime = thirdTime;
        this.fourthTime = fourthTime;
        this.products = products;
        this.orders = orders;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.spRepo = spRepo;
        this.timeRepository = timeRepository;
        this.demandLogRepository = demandLogRepository;
        this.fiveMinutesFromBeginning = fiveMinutesFromBeginning;
    }

    public void run() {
        calculateDemands();
        for (Product product : products) {
            divideDemandByProduct(
                    orders.stream()
                            .filter(order -> order.getProduct().getId().equals(product.getId()))
                            .collect(Collectors.toList()), product
            );
        }
    }

    public void divideDemandByProduct(List<FinalProductSellOrder> orders,
                                      Product product) {
        Time time = timeRepository.findById(1L).get();
        int demand = demands.get(product.getId());
        int demandToDivide = demand;
        int totalSold = 0;
        double totalBrandOnPrice;
        List<Double> brandOnPrices = new ArrayList<>();
        for (FinalProductSellOrder order : orders) {
            double brandOnPrice = 100.0 / order.getUnitPrice();
            brandOnPrices.add(brandOnPrice);
        }
        while (true) {
            totalBrandOnPrice = 0;
            for (int i = 0; i < orders.size(); i++) {
                // skip completed orders
                if (orders.get(i).getSoldQuantity().equals(orders.get(i).getQuantity())) continue;

                totalBrandOnPrice += brandOnPrices.get(i);
            }
            for (int i = 0; i < orders.size(); i++) {
                // skip completed orders
                if (orders.get(i).getSoldQuantity().equals(orders.get(i).getQuantity())) continue;

                int sellAmount = (int) Math.ceil((brandOnPrices.get(i) / totalBrandOnPrice) * demandToDivide);
                FinalProductSellOrder order = orders.get(i);
                if (sellAmount > order.getQuantity() - order.getSoldQuantity()) {
                    sellAmount = order.getQuantity() - order.getSoldQuantity();
                }
                order.setAcceptDate(new Date());
                order.setSoldQuantity(order.getSoldQuantity() + sellAmount);

                Team team = order.getSubmitter();
                team.setBalance(team.getBalance() + (sellAmount * order.getUnitPrice()));

                totalSold += sellAmount;
            }
            demandToDivide = demand - totalSold;
            if (demandToDivide <= 0 ||
                    orders.stream().allMatch(order -> order.getSoldQuantity().equals(order.getQuantity()))) {
                break;
            }
        }
        TimeResultDTO timeResultDTO = TimeUtil.getTime(time);
        List<StorageProduct> sps = new ArrayList<>();
        List<Log> logs = new ArrayList<>();
        orders.forEach(order -> {
            order.setClosed(true);
            Log log = new Log();
            log.setProduct(order.getProduct());
            log.setType(LogType.FINAL_SELL);
            log.setProductCount(Long.valueOf(order.getSoldQuantity()));
            log.setTeam(order.getSubmitter());
            log.setTotalCost(log.getProductCount() * order.getUnitPrice());
            log.setTimestamp(LocalDateTime.of(Math.toIntExact(timeResultDTO.getYear()),
                    Math.toIntExact(timeResultDTO.getMonth()),
                    Math.toIntExact(timeResultDTO.getDay()),
                    12,
                    34));
//            logRepository.save(log);
            logs.add(log);
            StorageProduct sp = TeamUtil.getSPFromProduct(order.getSubmitter(), order.getProduct()).get();
//            sp.setInStorageAmount(sp.getInStorageAmount() - order.getSoldQuantity());
//            sp.setBlockedAmount(sp.getBlockedAmount() - order.getQuantity());
//            sp.setSellableAmount(sp.getSellableAmount() - order.getSoldQuantity());
            TeamUtil.removeProductFromStorage(sp, order.getSoldQuantity());
            TeamUtil.removeProductFromBlock(sp, order.getQuantity());
//            TeamUtil.addProductToSellable(sp, order.getQuantity() - order.getSoldQuantity());
//            spRepo.save(sp);
            sps.add(sp);
        });

        logRepository.saveAll(logs);
        spRepo.saveAll(sps);
        finalProductSellOrderRepository.saveAll(orders);
    }

    private void calculateDemands() {
        int firstEraDemand = totalDemand;
        int secondEraDemand = 0;
        int thirdEraDemand = 0;
        int fourthEraDemand = 0;
        int fifthEraDemand = 0;

        if (firstTime != null) {
            long timePassed = Duration.between(firstTime, LocalDateTime.now(ZoneOffset.UTC)).toMinutes() / 5;
            while (timePassed != 0) {
                firstEraDemand *= Math.pow(0.97, 0.35 * timePassed);
                timePassed -= 1;
            }
            secondEraDemand = totalDemand - firstEraDemand;
        }
        if (secondTime != null) {
            long timePassed = Duration.between(secondTime, LocalDateTime.now(ZoneOffset.UTC)).toMinutes() / 5; //
            while (timePassed != 0) {
                secondEraDemand *= Math.pow(0.92, 0.1 * timePassed);
                timePassed -= 1;
            }
            thirdEraDemand = totalDemand - secondEraDemand - firstEraDemand;
        }
        if (thirdTime != null) {
            long timePassed = Duration.between(thirdTime, LocalDateTime.now(ZoneOffset.UTC)).toMinutes() / 5;
            while (timePassed != 0) {
                thirdEraDemand *= Math.pow(0.92, 0.1 * timePassed);
                timePassed -= 1;
            }
            fourthEraDemand = totalDemand - thirdEraDemand - secondEraDemand - firstEraDemand;
        }
        if (fourthTime != null) {
            long timePassed = Duration.between(fourthTime, LocalDateTime.now(ZoneOffset.UTC)).toMinutes() / 5; //
            while (timePassed != 0) {
                fourthEraDemand *= Math.pow(0.92, 0.1 * timePassed);
                timePassed -= 1;
            }
            fifthEraDemand = totalDemand - fourthEraDemand - thirdEraDemand - secondEraDemand - firstEraDemand;
        }
        HashMap<Long, Integer> eraDemand = new HashMap<>();
        System.out.println("first era demand: " + firstEraDemand);
        System.out.println("second era demand: " + secondEraDemand);
        System.out.println("third era demand: " + thirdEraDemand);
        System.out.println("fourth era demand: " + fourthEraDemand);
        System.out.println("fifth era demand: " + fifthEraDemand);
        eraDemand.put(0L, firstEraDemand);
        eraDemand.put(1163L, secondEraDemand);
        eraDemand.put(2738L, thirdEraDemand);
        eraDemand.put(6105L, fourthEraDemand);
        eraDemand.put(7680L, fifthEraDemand);

        StringBuilder productDemands = new StringBuilder();
        for (Product p : products) {
            demands.put(p.getId(), (int) (p.getDemandCoefficient() * eraDemand.get(p.getAvailableDay())));
            productDemands.append(p.getPrettyName()).append(": ").append(p.getDemandCoefficient() * eraDemand.get(p.getAvailableDay())).append(";\n");
            System.out.println("demand for " + p.getName() + " is: " + (int) (p.getDemandCoefficient() * eraDemand.get(p.getAvailableDay())));
        }
        DemandLog log = new DemandLog();
        log.setTotalDemand(totalDemand);
        log.setFirstEraDemand(firstEraDemand);
        log.setSecondEraDemand(secondEraDemand);
        log.setThirdEraDemand(thirdEraDemand);
        log.setFourthEraDemand(fourthEraDemand);
        log.setFifthEraDemand(fifthEraDemand);
        log.setProductDemands(productDemands.toString());
        log.setTime(LocalDateTime.now(ZoneOffset.UTC));
        log.setDemandId(fiveMinutesFromBeginning);
        demandLogRepository.save(log);
    }
}
