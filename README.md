# BidMart Group Project
- Group Name: PT.KITABISA
- Group ID: Group 3

## Members
| Name | NPM | Role |
|------|-----|------|
| Ayshia La Fleur Felizia | 2406365351 | PIC Authentication |
| Rafasyah Miyauchi | 2406453581 | PIC Auction |
| Alifa Izzatunissa Elqudsi Prabowo | 2406365212 | PIC Catalogue |
| Roben Joseph Buce Tambayong | 2406453594 | PIC Wallet Management |
| Bagas Zharif Prasetyo | 2406453423 | PIC Order |

## Overview
BidMart is a real-time auction and marketplace platform where users can auction and bid on items securely. The system supports high-frequency, concurrent bidding involving hundreds or thousands of users while also providing traditional marketplace features such as catalogue browsing, user management, and order fulfillment.

The system prioritizes:

- Bid integrity → Bids must not be lost or reordered.
- Real-time responsiveness → Users must see the latest bid updates quickly.
- Monetary consistency → User balances must remain accurate, with funds properly held during active bids.

## Module 
### 1. Authentication & User Management
- User registration and login
- Token lifecycle management (access & refresh tokens)
- Two-factor authentication (2FA)
- Session management and revocation

### 2. Auction & Bidding
- Bidding
- Ensuring the order of incoming bids
- Extension of auction period
- Determining the winner of an auction

### 3. Catalogue & Listing Management
- Create and manage listings
- Organize hierarchical categories
- Support filtering and browsing
- Display seller profile information

### 4. Wallet & Balance Management
- Track available and held balances
- Deposit and withdraw funds
- Convert held funds into payment for winners
- Maintain complete transaction history

### 5. Order & Notification
- Create orders for auction winners
- Manage order lifecycle (created → shipped → completed)
- Provide shipment tracking
- Deliver notifications (bid placed, outbid, winner, balance updates)

## Milestone Roadmap.
### Preparation 
**Goal** Project setup + DevOps foundation
- Set up GitHub organization and module-based repository structure.
- Define branching strategy (main, staging, feat/*).
- Implement feature branch workflow with PR reviews.
- Configure CI (build + tests + quality checks).
- 
### Milestone 1 — 25%
**Goal:** minimal vertical slice + module scaffolding
-  Implement **one** feature that proves integration: frontend ↔ backend ↔ database
-  Each module has skeleton: controller/service/repository/entity/dto
-  Establish event contracts (even if mocked)
-  Add unit tests for the slice

### Milestone 2 — 50%
**Goal:** core flows begin working end-to-end
-  Auth: register/login + token validation middleware
-  Catalogue: create listing + browse listing
-  Wallet: top-up + view balance
-  Auction: create auction + place bid (basic)
-  Event publishing (BidPlaced, WinnerDetermined) + mock subscribers

### Milestone 3 — 75%
**Goal:** realism + correctness + quality gates
-  Auction constraints (deadline extension, fair tie-breaking)
-  Wallet holds for bids + release/convert-to-payment
-  Catalogue price updates based on bids (async ok)
-  Increase code coverage target (e.g., 80%+)
-  Observability basics (logging, health check endpoint)

### Milestone 4 — 100% (Final)
**Goal:** integrated system, hardened quality, ready demo
-  End-to-end happy path demo: browse → bid → win → payment → (order optional)
-  Robust CI/CD: tests + quality checks block deploy
-  Performance / concurrency considerations for bidding (basic)
-  Documentation: APIs + runbook + architecture decisions


## Users / Roles
### Administrator
Oversees platform operations, manages users and permissions, moderates listings, resolves disputes, and has full system access.
### Seller
Creates auction listings with price, reserve, duration, and item details. Monitors bids and fulfills orders after the auction ends.
### Buyer
Browses listings, places bids (including proxy bids), manages wallet balance, and completes payment after winning an auction.

## Git Repo: 
https://github.com/advprog-2026-kki3-project/group-preparation/tree/main

