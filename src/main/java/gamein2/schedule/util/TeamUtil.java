package gamein2.schedule.util;

import gamein2.schedule.exception.BadRequestException;
import gamein2.schedule.model.entity.*;
import gamein2.schedule.model.enums.ShippingMethod;
import gamein2.schedule.model.repository.StorageProductRepository;
import gamein2.schedule.model.repository.TeamRepository;

import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class TeamUtil {

    public static int calculateStorageSpace(Team team) {
        return team.getIsStorageUpgraded() ? 75_000_000 : 50_000_000;
    }
    public static int calculateAvailableSpace(Team team) {
        int result = calculateStorageSpace(team);
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            long unitVolume = storageProduct.getProduct().getUnitVolume();
            result -= storageProduct.getInStorageAmount() * unitVolume;
            result -= storageProduct.getManufacturingAmount() * unitVolume;
        }
        return result;
    }

    public static int calculateUsedSpace(Team team) {
        int result = 0;
        for (StorageProduct storageProduct : team.getStorageProducts()) {
            result += storageProduct.getInStorageAmount() * storageProduct.getProduct().getUnitVolume();
        }
        return result;
    }

    public static int calculateManufacturing(Team team) {
        int result = 0;
        for (StorageProduct sp : team.getStorageProducts()) {
            result += sp.getManufacturingAmount() * sp.getProduct().getUnitVolume();
        }
        return result;
    }

    public static StorageProduct addProductToRoute(StorageProduct sp, Integer amount) {
        sp.setInRouteAmount(sp.getInRouteAmount() + amount);

        return sp;
    }

    public static StorageProduct addProductToStorage(StorageProduct sp, Integer amount) {
        sp.setInStorageAmount(sp.getInStorageAmount() + amount);

        return sp;
    }

    public static StorageProduct blockProductInStorage(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getInStorageAmount() - sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() + amount);

        return sp;
    }

    public static StorageProduct unblockProduct(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromBlockedAndStorage(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getBlockedAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() - amount);
        sp.setInStorageAmount(sp.getInStorageAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromStorage(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getInStorageAmount() < amount) {
            throw new BadRequestException("شما مقدار کافی " + sp.getProduct().getName() + " ندارید!");
        }

        sp.setInStorageAmount(sp.getInStorageAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromInRoute(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getInRouteAmount() < amount) {
            throw new BadRequestException("این مقدار " + sp.getProduct().getName() + " در مسیر نیست!");
        }

        sp.setInRouteAmount(sp.getInRouteAmount() - amount);

        return sp;
    }

    public static StorageProduct removeProductFromBlocked(StorageProduct sp, Integer amount)
            throws BadRequestException {
        if (sp.getBlockedAmount() < amount) {
            throw new BadRequestException("این مقدار " + sp.getProduct().getName() + " در مسیر نیست!");
        }

        sp.setBlockedAmount(sp.getBlockedAmount() - amount);

        return sp;
    }

    public static StorageProduct getSPFromProduct(Team team, Product product,
                                                  StorageProductRepository storageProductRepository)
            throws BadRequestException {
        Optional<StorageProduct> storageProductOptional = storageProductRepository.findFirstByProduct_IdAndTeam_Id(product.getId(),
                team.getId());
        if (storageProductOptional.isEmpty()) {
            throw new BadRequestException("شما مقدار کافی " + product.getName() + " ندارید!");
        }
        return storageProductOptional.get();
    }

    public static StorageProduct getOrCreateSPFromProduct(Team team, Product product,
                                                          StorageProductRepository storageProductRepository,
                                                          TeamRepository teamRepository) {
        Optional<StorageProduct> storageProductOptional = storageProductRepository.findFirstByProduct_IdAndTeam_Id(product.getId(),
                team.getId());
        if (storageProductOptional.isPresent()) {
            return storageProductOptional.get();
        } else {
            StorageProduct sp = new StorageProduct();
            sp.setProduct(product);
            sp.setTeam(team);

            storageProductRepository.save(sp);
            team.getStorageProducts().add(sp);
            teamRepository.save(team);

            return sp;
        }
    }

    public static int calculateShippingPrice(ShippingMethod method, int distance, int volume) {
        int price = 10000 + 100 * (int) pow(distance * volume, 0.5);
        return method == ShippingMethod.SAME_REGION ? 10000 : method == ShippingMethod.SHIP ?
                price : 3 * price;
    }

    public static int calculateShippingDuration(ShippingMethod method, int distance) {
        return method == ShippingMethod.SAME_REGION ? 0 : method == ShippingMethod.SHIP ?
                distance * 3 * 60 : distance * 60;
    }
}