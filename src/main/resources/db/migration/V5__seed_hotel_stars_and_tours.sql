-- V5__seed_hotel_stars_and_tours.sql: Seed 3-star, 4-star, and 5-star hotels and link tours
INSERT INTO hotels (id, name, country, city, address, stars, description, amenities, image_url, created_at, updated_at) VALUES
(6, 'Cappadocia Cave Suites', 'Турция', 'Гёреме', 'Goreme Valley 12, Goreme', 3,
 'Уютный аутентичный отель в пещерах национального парка Гёреме с террасой с видом на взлетающие воздушные шары.',
 'Панорамная терраса, Традиционные завтраки, Экскурсионное бюро, Wi-Fi',
 'https://images.unsplash.com/photo-1570939274717-7eda259b50ed?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(7, 'Karon Princess Hotel', 'Таиланд', 'Пхукет', 'Karon Beach Road 45, Phuket', 3,
 'Комфортный пляжный отель в 50 метрах от белоснежного песчаного пляжа Карон. Бассейн в окружении тропической зелени.',
 'Первая линия, Открытый бассейн, Ресторан тайской кухни, Спа, Wi-Fi',
 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(8, 'Sultanahmet Palace Hotel', 'Турция', 'Стамбул', 'Torun Sokak 19, Sultanahmet, Istanbul', 4,
 'Элегантный отель в самом сердце исторического Стамбула с панорамным рестораном с видом на Голубую мечеть и пролив Босфор.',
 'Вид на Босфор, Ресторан османской кухни, Хаммам, Экскурсии, Wi-Fi',
 'https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(9, 'Adaaran Club Rannalhi', 'Мальдивы', 'Мале', 'South Male Atoll, Maldives', 5,
 'Живописный остров-курорт с водными бунгало, белоснежной песчаной косой и домашним рифом с морскими черепахами.',
 'Водные виллы, All Inclusive, Снорклинг-центр, Спа Chavana Spa, Бар на закате',
 'https://images.unsplash.com/photo-1514282401047-d79a71a590e8?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(10, 'Emirates Palace Mandarin Oriental', 'ОАЭ', 'Абу-Даби', 'West Corniche Road, Abu Dhabi', 5,
 'Легендарный восточный дворец-отель с роскошными интерьерами из сусального золота, километровым частным пляжем и королевским сервисом.',
 'Частный пляж 1.3 км, Пристань для яхт, 14 ресторанов, Спа-комплекс, Консьерж',
 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

UPDATE tours SET hotel_id = 8, available_slots = 10 WHERE id = 6;
UPDATE tours SET hotel_id = 7, available_slots = 14 WHERE id = 7;
UPDATE tours SET hotel_id = 9, available_slots = 10 WHERE id = 8;
UPDATE tours SET hotel_id = 6, available_slots = 8 WHERE id = 9;
UPDATE tours SET hotel_id = 10, available_slots = 11 WHERE id = 10;
