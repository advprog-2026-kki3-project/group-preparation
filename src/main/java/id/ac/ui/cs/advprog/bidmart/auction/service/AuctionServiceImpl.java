package id.ac.ui.cs.advprog.bidmart.auction.service;

import id.ac.ui.cs.advprog.bidmart.auction.dto.AuctionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.BidResponseDTO;
import id.ac.ui.cs.advprog.bidmart.auction.dto.CreateAuctionRequestDTO;
import id.ac.ui.cs.advprog.bidmart.auction.model.Auction;
import id.ac.ui.cs.advprog.bidmart.auction.model.AuctionStage;
import id.ac.ui.cs.advprog.bidmart.auction.model.Bid;
import id.ac.ui.cs.advprog.bidmart.auction.repository.AuctionRepository;
import id.ac.ui.cs.advprog.bidmart.auction.repository.BidRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository; // NEW

    public AuctionServiceImpl(BidRepository bidRepository, AuctionRepository auctionRepository) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
    }

    @Override
    public List<BidResponseDTO> getBiddingHistory(String auctionId) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);
        return bids.stream()
                .map(bid -> new BidResponseDTO(bid.getBidderId(), bid.getAmount(), bid.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Override
    public AuctionResponseDTO createAuction(CreateAuctionRequestDTO request) {
        // 1. This is to map the incoming Request DTO to our Database Entity
        Auction auction = new Auction();
        auction.setSellerId(request.getSellerId());
        auction.setCatalogueListingId(request.getCatalogueListingId());
        auction.setInitialPrice(request.getInitialPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());

        // 2. Setting the default system values for a brand new auction
        auction.setStage(AuctionStage.DRAFT);
        auction.setCurrentHighestBid(0.0);

        // 3. Then saving it to the database.
        Auction savedAuction = auctionRepository.save(auction);

        // 4. This should map the saved Entity back to a Response DTO
        return new AuctionResponseDTO(
                savedAuction.getId(),
                savedAuction.getSellerId(),
                savedAuction.getCatalogueListingId(),
                savedAuction.getInitialPrice(),
                savedAuction.getReservePrice(),
                savedAuction.getCurrentHighestBid(),
                savedAuction.getStartTime(),
                savedAuction.getEndTime(),
                savedAuction.getStage()
        );
    }
}