package gamein2.schedule.util;

import gamein2.schedule.exception.BadRequestException;
import gamein2.schedule.model.dto.TimeResultDTO;
import gamein2.schedule.model.entity.*;
import gamein2.schedule.model.enums.LogType;
import gamein2.schedule.model.repository.FinalProductSellOrderRepository;
import gamein2.schedule.model.repository.LogRepository;
import gamein2.schedule.model.repository.StorageProductRepository;
import gamein2.schedule.model.repository.TimeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GameinTradeTasks {

    private final LogRepository logRepository;
    private final HashMap<Long, Integer> demands = new HashMap<>();
    private final HashMap<Long, Double> brands = new HashMap<>();
    private final int totalDemand;
    private final Date firstTime;
    private final Date secondTime;
    private final Date thirdTime;
    private final Date fourthTime;
    private final List<Product> products;
    private final List<FinalProductSellOrder> orders;
    private final List<Team> teams;
    private final FinalProductSellOrderRepository finalProductSellOrderRepository;
    private final StorageProductRepository spRepo;
    private HashMap<Long, Double> prevBrandsMap = null;
    private HashMap<Long, Double> prevPrevBrandsMap = null;

    private final TimeRepository timeRepository;

    public GameinTradeTasks(List<Brand> previousBrands, List<Brand> previousPreviousBrands, LogRepository logRepository, int totalDemand,
                            Date firstTime, Date secondTime, Date thirdTime, Date fourthTime, List<Product> products,
                            List<FinalProductSellOrder> orders, List<Team> teams,
                            FinalProductSellOrderRepository finalProductSellOrderRepository,
                            StorageProductRepository spRepo, TimeRepository timeRepository) {
        this.logRepository = logRepository;
        this.totalDemand = totalDemand;
        this.firstTime = firstTime;
        this.secondTime = secondTime;
        this.thirdTime = thirdTime;
        this.fourthTime = fourthTime;
        this.products = products;
        this.orders = orders;
        this.teams = teams;
        this.finalProductSellOrderRepository = finalProductSellOrderRepository;
        this.spRepo = spRepo;
        this.timeRepository = timeRepository;

        if (previousPreviousBrands.size() > 0) {
            prevPrevBrandsMap = new HashMap<>();
            for (Brand brand : previousBrands) {
                prevPrevBrandsMap.put(brand.getTeam().getId(), brand.getBrand());
            }
        }
        if (previousBrands.size() > 0) {
            prevBrandsMap = new HashMap<>();
            for (Brand brand : previousBrands) {
                prevBrandsMap.put(brand.getTeam().getId(), brand.getBrand());
            }
        }
    }

    public HashMap<Long, Double> run() {
        calculateDemands();
        calculateBrands();
        for (Product product : products) {
            divideDemandByProduct(
                    orders.stream()
                            .filter(order -> order.getProduct().getId().equals(product.getId()))
                            .collect(Collectors.toList()), product
            );
        }
        return brands;
    }

    public void divideDemandByProduct(List<FinalProductSellOrder> orders,
                                      Product product) {
        Time time = timeRepository.findById(1L).get();
        int demand = demands.get(product.getId());
        int totalSold = 0;
        double totalBrandOnPrice;
        List<Double> brandOnPrices = new ArrayList<>();
        for (FinalProductSellOrder order : orders) {
            double brand = brands.get(order.getSubmitter().getId());
            double brandOnPrice = brand / order.getQuantity();
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

                int sellAmount = (int) Math.ceil((brandOnPrices.get(i) / totalBrandOnPrice) * demand);
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
            if (totalSold >= demand ||
                    orders.stream().allMatch(order -> order.getSoldQuantity().equals(order.getQuantity()))) {
                break;
            }
        }
        TimeResultDTO timeResultDTO = TimeUtil.getTime(time);
        orders.forEach(order -> {
            order.setClosed(true);
            try {
                StorageProduct sp = TeamUtil.removeProductFromStorage(
                        TeamUtil.getSPFromProduct(order.getSubmitter(), order.getProduct(), spRepo),
                        order.getSoldQuantity()
                );
                TeamUtil.removeProductFromBlocked(sp, order.getQuantity());
                spRepo.save(sp);
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
                        23));
                logRepository.save(log);
            } catch (BadRequestException e) {
                System.err.println(e.getMessage());
            }
        });

        finalProductSellOrderRepository.saveAll(orders);
    }

    private void calculateDemands() {
        int firstEraDemand = (int) (totalDemand * timeRepository.findById(1L).get().getDemandScale());
        int secondEraDemand = 0;
        int thirdEraDemand = 0;
        int fourthEraDemand = 0;
        int fifthEraDemand = 0;

        if (firstTime != null) {
            int timePassed = (int) ((new Date().getTime() - firstTime.getTime()) / (1 * 60 * 1000));
            while (timePassed != 0) {
                firstEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            secondEraDemand = totalDemand - firstEraDemand;
        }
        if (secondTime != null) {
            int timePassed = (int) ((new Date().getTime() - secondTime.getTime()) / (1 * 60 * 1000));
            while (timePassed != 0) {
                secondEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            thirdEraDemand = totalDemand - secondEraDemand - firstEraDemand;
        }
        if (thirdTime != null) {
            int timePassed = (int) ((new Date().getTime() - thirdTime.getTime()) / (1 * 60 * 1000));
            while (timePassed != 0) {
                thirdEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            fourthEraDemand = totalDemand - thirdEraDemand - secondEraDemand - firstEraDemand;
        }
        if (fourthTime != null) {
            int timePassed = (int) ((new Date().getTime() - fourthTime.getTime()) / (1 * 60 * 1000));
            while (timePassed != 0) {
                fourthEraDemand *= 0.99; // TODO change this to a better function
                timePassed -= 1;
            }
            fifthEraDemand = totalDemand - fourthEraDemand - thirdEraDemand - secondEraDemand - firstEraDemand;
        }
        HashMap<Integer, Integer> eraDemand = new HashMap<>();
        System.out.println("first era demand: " + firstEraDemand);
        System.out.println("second era demand: " + secondEraDemand);
        System.out.println("third era demand: " + thirdEraDemand);
        System.out.println("fourth era demand: " + fourthEraDemand);
        System.out.println("fifth era demand: " + fifthEraDemand);
        eraDemand.put(0, firstEraDemand);
        eraDemand.put(1163, secondEraDemand);
        eraDemand.put(2738, thirdEraDemand);
        eraDemand.put(4688, fourthEraDemand);
        eraDemand.put(7425, fifthEraDemand);

        for (Product p : products) {
            demands.put(p.getId(), (int) (p.getDemandCoefficient() * eraDemand.get(p.getAvailableDay())));
            System.out.println("demand for " + p.getName() + " is: " + (int) (p.getDemandCoefficient() * eraDemand.get(p.getAvailableDay())));
        }
    }

    private void calculateBrands() {
//        double alpha = -1.0;
//        double betta = -1.0;
//        double theta = 1.0;
//        double lambda = 1.0;
//
//        HashMap<Long, Double> formulae = new HashMap<>();
//        double sumFormulae = 0;
//        for (Team team : teams) {
//            Long totalSellAmount = finalProductSellOrderRepository.totalSoldAmount(team.getId());
//            if (totalSellAmount == null) totalSellAmount = 0L;
//            double formula =
//                    alpha * Math.pow(getCarbonFootprint(), lambda) +
//                            betta * calculateSecondBrandFactor(team.getId(), totalSellAmount) +
//                            theta * totalSellAmount;
//            formulae.put(team.getId(), formula);
//            sumFormulae += formula;
//        }
        for (Team team : teams) {
            brands.put(team.getId(), 50.0);
        }
    }

//    private double calculateSecondBrandFactor(Long teamId, Long totalSellAmount) {
//        double result = 0;
//        for (Long productId : finalProductSellOrderRepository.findProduct_IdsByTeam_Id(teamId)) {
//            double deltaBrand = meanDeltaBrand(productId);
//            if (deltaBrand == 0) {
//                continue;
//            }
//            Long sellAmount = finalProductSellOrderRepository.totalProductSoldAmount(teamId, productId);
//            if (sellAmount == null) sellAmount = 0L;
//            result += ((double) sellAmount / totalSellAmount) * deltaBrand;
//        }
//        return result;
//    }
//
//    private double meanDeltaBrand(Long productId) {
//        if (prevBrandsMap == null || prevPrevBrandsMap == null) {
//            return 0;
//        }
//        List<Long> teamIds = finalProductSellOrderRepository.findTeam_IdsByProduct_Id(productId);
//        double sumDeltaBrand = 0;
//        for (Long teamId : teamIds) {
//            sumDeltaBrand += prevBrandsMap.get(teamId) - prevPrevBrandsMap.get(teamId);
//        }
//        return sumDeltaBrand / teamIds.size();
//    }

//    private int getCarbonFootprint() {
//        return 50;
//    }
}
