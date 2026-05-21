-- 1. ROOT CATEGORIES (No Parent)
INSERT INTO category (id, name, parent_category_id) VALUES ('10000000-0000-0000-0000-000000000000', 'Electronics', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('20000000-0000-0000-0000-000000000000', 'Fashion & Apparel', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('30000000-0000-0000-0000-000000000000', 'Home & Garden', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('40000000-0000-0000-0000-000000000000', 'Automotive', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('50000000-0000-0000-0000-000000000000', 'Collectibles & Art', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('60000000-0000-0000-0000-000000000000', 'Sports & Outdoors', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('70000000-0000-0000-0000-000000000000', 'Toys & Hobbies', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('80000000-0000-0000-0000-000000000000', 'Health & Beauty', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('90000000-0000-0000-0000-000000000000', 'Books, Movies & Music', NULL);
INSERT INTO category (id, name, parent_category_id) VALUES ('a0000000-0000-0000-0000-000000000000', 'Business & Industrial', NULL);

-- 2. SUBCATEGORIES (Depth Level 2)

-- Under Electronics
INSERT INTO category (id, name, parent_category_id) VALUES ('11000000-0000-0000-0000-000000000000', 'Computers & Laptops', '10000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('12000000-0000-0000-0000-000000000000', 'Mobile Phones & Accessories', '10000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('13000000-0000-0000-0000-000000000000', 'Cameras & Photography', '10000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('14000000-0000-0000-0000-000000000000', 'TV, Video & Audio', '10000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('15000000-0000-0000-0000-000000000000', 'Video Games & Consoles', '10000000-0000-0000-0000-000000000000');

-- Under Fashion & Apparel
INSERT INTO category (id, name, parent_category_id) VALUES ('21000000-0000-0000-0000-000000000000', 'Women''s Clothing', '20000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('22000000-0000-0000-0000-000000000000', 'Men''s Clothing', '20000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('23000000-0000-0000-0000-000000000000', 'Shoes & Sneakers', '20000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('24000000-0000-0000-0000-000000000000', 'Watches & Jewelry', '20000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('25000000-0000-0000-0000-000000000000', 'Bags & Accessories', '20000000-0000-0000-0000-000000000000');

-- Under Home & Garden
INSERT INTO category (id, name, parent_category_id) VALUES ('31000000-0000-0000-0000-000000000000', 'Furniture', '30000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('32000000-0000-0000-0000-000000000000', 'Major Appliances', '30000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('33000000-0000-0000-0000-000000000000', 'Tools & Workshop Equipment', '30000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('34000000-0000-0000-0000-000000000000', 'Home Decor', '30000000-0000-0000-0000-000000000000');

-- Under Automotive
INSERT INTO category (id, name, parent_category_id) VALUES ('41000000-0000-0000-0000-000000000000', 'Cars & Trucks', '40000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('42000000-0000-0000-0000-000000000000', 'Motorcycles', '40000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('43000000-0000-0000-0000-000000000000', 'Parts & Accessories', '40000000-0000-0000-0000-000000000000');

-- Under Collectibles & Art
INSERT INTO category (id, name, parent_category_id) VALUES ('51000000-0000-0000-0000-000000000000', 'Trading Card Games', '50000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('52000000-0000-0000-0000-000000000000', 'Antiques', '50000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('53000000-0000-0000-0000-000000000000', 'Art Prints & Paintings', '50000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('54000000-0000-0000-0000-000000000000', 'Comics & Memorabilia', '50000000-0000-0000-0000-000000000000');

-- Under Sports & Outdoors
INSERT INTO category (id, name, parent_category_id) VALUES ('61000000-0000-0000-0000-000000000000', 'Fitness & Running', '60000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('62000000-0000-0000-0000-000000000000', 'Camping & Hiking', '60000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('63000000-0000-0000-0000-000000000000', 'Cycling', '60000000-0000-0000-0000-000000000000');

-- Under Toys & Hobbies
INSERT INTO category (id, name, parent_category_id) VALUES ('71000000-0000-0000-0000-000000000000', 'Action Figures', '70000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('72000000-0000-0000-0000-000000000000', 'Board Games', '70000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('73000000-0000-0000-0000-000000000000', 'Building Toys (LEGO)', '70000000-0000-0000-0000-000000000000');

-- Under Health & Beauty
INSERT INTO category (id, name, parent_category_id) VALUES ('81000000-0000-0000-0000-000000000000', 'Fragrances', '80000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('82000000-0000-0000-0000-000000000000', 'Skincare', '80000000-0000-0000-0000-000000000000');

-- Under Books, Movies & Music
INSERT INTO category (id, name, parent_category_id) VALUES ('91000000-0000-0000-0000-000000000000', 'Fiction Books', '90000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('92000000-0000-0000-0000-000000000000', 'Non-Fiction', '90000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('93000000-0000-0000-0000-000000000000', 'Vinyl Records', '90000000-0000-0000-0000-000000000000');

-- 3. SUB-SUBCATEGORIES (Depth Level 3)

-- Under Computers & Laptops
INSERT INTO category (id, name, parent_category_id) VALUES ('11100000-0000-0000-0000-000000000000', 'Gaming Laptops', '11000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('11200000-0000-0000-0000-000000000000', 'MacBooks', '11000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('11300000-0000-0000-0000-000000000000', 'PC Components (GPUs/CPUs)', '11000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('11400000-0000-0000-0000-000000000000', 'Mechanical Keyboards', '11000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('11500000-0000-0000-0000-000000000000', 'Monitors', '11000000-0000-0000-0000-000000000000');

-- Under Mobile Phones
INSERT INTO category (id, name, parent_category_id) VALUES ('12100000-0000-0000-0000-000000000000', 'Smartphones (iOS)', '12000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('12200000-0000-0000-0000-000000000000', 'Smartphones (Android)', '12000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('12300000-0000-0000-0000-000000000000', 'Smartwatches', '12000000-0000-0000-0000-000000000000');

-- Under TV, Video & Audio
INSERT INTO category (id, name, parent_category_id) VALUES ('14100000-0000-0000-0000-000000000000', 'Headphones & Earbuds', '14000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('14200000-0000-0000-0000-000000000000', 'Smart TVs', '14000000-0000-0000-0000-000000000000');

-- Under Shoes & Sneakers
INSERT INTO category (id, name, parent_category_id) VALUES ('23100000-0000-0000-0000-000000000000', 'Men''s Sneakers', '23000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('23200000-0000-0000-0000-000000000000', 'Women''s Sneakers', '23000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('23300000-0000-0000-0000-000000000000', 'Formal Shoes', '23000000-0000-0000-0000-000000000000');

-- Under Trading Card Games
INSERT INTO category (id, name, parent_category_id) VALUES ('51100000-0000-0000-0000-000000000000', 'Pokémon TCG', '51000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('51200000-0000-0000-0000-000000000000', 'Magic: The Gathering', '51000000-0000-0000-0000-000000000000');
INSERT INTO category (id, name, parent_category_id) VALUES ('51300000-0000-0000-0000-000000000000', 'Yu-Gi-Oh!', '51000000-0000-0000-0000-000000000000');