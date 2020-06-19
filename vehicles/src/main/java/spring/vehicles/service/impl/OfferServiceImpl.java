package spring.vehicles.service.impl;

import spring.vehicles.dao.BrandRepository;
import spring.vehicles.dao.OfferRepository;
import spring.vehicles.dao.UserRepository;
import spring.vehicles.exception.EntityNotFoundException;
import spring.vehicles.exception.InvalidEntityException;
import spring.vehicles.model.Model;
import spring.vehicles.model.Offer;
import spring.vehicles.model.User;
import spring.vehicles.service.OfferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OfferServiceImpl implements OfferService {

    @Autowired
    private OfferRepository offerRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BrandRepository brandRepo;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Collection<Offer> getOffers() {
        return offerRepo.findAll();
    }

    @Override
    public Offer getOfferById(Long id) {
        return offerRepo.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Offer with ID=%s not found.", id)));
    }

    @Override
    public Offer createOffer(@Valid Offer offer) {
        Long sellerId;
        if (offer.getSeller() != null && offer.getSeller().getId() != null) {
            sellerId = offer.getSeller().getId();
        } else {
            sellerId = offer.getSellerId();
        }
        if (sellerId != null) {
            User author = userRepo.findById(sellerId)
                    .orElseThrow(() -> new InvalidEntityException("Seller with ID=" + sellerId + " does not exist."));
            offer.setSeller(author);
        }

        Long modelId;
        if (offer.getModel() != null && offer.getModel().getId() != null) {
            modelId = offer.getModel().getId();
        } else {
            modelId = offer.getModelId();
        }
        if (modelId != null) {
            Model model = brandRepo.findModelById(modelId)
                    .orElseThrow(() -> new InvalidEntityException("Model with ID=" + modelId + " does not exist."));
            offer.setModel(model);
        }

        if (offer.getCreated() == null) {
            offer.setCreated(new Date());
        }
        offer.setModified(offer.getCreated());

        return offerRepo.save(offer);
    }

    @Override
    public Offer updateOffer(Offer offer) {
        offer.setModified(new Date());
        Offer old = getOfferById(offer.getId());
        if (old == null) {
            throw new EntityNotFoundException(String.format("Offer with ID=%s not found.", offer.getId()));
        }
        if (offer.getSeller() != null && offer.getSeller().getId() != old.getSeller().getId())
            throw new InvalidEntityException("Seller of offer could not be changed");
        offer.setSeller(old.getSeller());
        return offerRepo.save(offer);
    }

    @Override
    public Offer deleteOffer(Long id) {
        Offer old = offerRepo.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Offer with ID=%s not found.", id)));
        offerRepo.deleteById(id);
        return old;
    }

    @Override
    public long getOffersCount() {
        return offerRepo.count();
    }

    // Declarative transaction
    @Transactional
    public List<Offer> createOffersBatch(List<Offer> offers) {
        List<Offer> created = offers.stream()
                .map(offer -> {
                    Offer resultOffer = createOffer(offer);
                    return resultOffer;
                }).collect(Collectors.toList());
        return created;
    }
}