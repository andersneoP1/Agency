-- V3__seed_catalog.sql: Seed hotels, tours, flights and promo codes

-- 1. Seed Hotels (5 hotels)
INSERT INTO hotels (id, name, country, city, address, stars, description, amenities, image_url, created_at, updated_at) VALUES
(1, 'Rixos Premium Belek', 'Турция', 'Белек', 'Ileribasi Mevkii, Belek', 5, 
 'Роскошный курортный комплекс на первой береговой линии Средиземного моря с собственным пляжем протяженностью 700 метров, спа-комплексом Anjana Spa и аквапарком.',
 'Собственный пляж, Ultra All Inclusive, Бассейны с подогревом, Спа-центр, Wi-Fi, Детский клуб, Теннисные корты, Фитнес-центр',
 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'Atlantis The Palm', 'ОАЭ', 'Дубай', 'Crescent Rd, The Palm Jumeirah, Dubai', 5,
 'Знаменитый отель-дворец на насыпном острове Palm Jumeirah. Всемирно известный аквапарк Aquaventure, гигантский океанариум Lost Chambers и рестораны от шеф-поваров со звездами Мишлен.',
 'Аквапарк Aquaventure, Океанариум, Частный пляж, 23 ресторана, Вертолетная площадка, Бутики, Спа-салон ShuiQi, Консьерж-сервис',
 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Pickalbatros Dana Beach Resort', 'Египет', 'Хургада', 'Sahl Hasheesh Road, Hurghada', 5,
 'Превосходный пятизвездочный курорт с собственной лагуной, по которой курсируют лодки к пляжу Красного моря. Отличный риф для сноркелинга и дайвинга.',
 'Первая линия, Песчаный пляж, 5 открытых бассейнов, Водные горки, 6 ресторанов a la carte, Дайвинг-центр, Анимация',
 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, 'Ayana Resort and Spa Bali', 'Индонезия', 'Бали', 'Karang Mas Estate, Jimbaran, Bali', 5,
 'Уединенный тропический отель на скале Джимбаран с панорамным видом на Индийский океан, легендарным баром Rock Bar и акватоническим бассейном.',
 'Панорамный инфинити-бассейн, Rock Bar, Частный пляж Кубу, Талассотерапия, Поле для гольфа, Бесплатный шаттл',
 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(5, 'Grand Hotel Zell am See', 'Австрия', 'Целль-ам-Зее', 'Esplanade 4, 5700 Zell am See', 4,
 'Исторический отель на живописном полуострове прямо на берегу озера Целль в окружении альпийских вершин. Идеален для зимнего катания и летнего трекинга.',
 'Вид на озеро, Спа Classic Spa, Крытый бассейн, Сауна, Близость к подъемникам, Прокат лыж, Ресторан тирольской кухни',
 'https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=1200&q=80',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Seed Tours (10 tours)
INSERT INTO tours (id, title, description, country, city, hotel_id, price, start_date, end_date, duration_days, included_services, image_url, hot_offer, available_slots, created_at, updated_at) VALUES
(1, 'Жемчужина Антальи: Rixos Belek All Inclusive',
 'Незабываемый 7-дневный отдых на средиземноморском побережье Белека в премиальном отеле Rixos. В стоимость включен перелет, трансфер бизнес-класса, питание All Inclusive и посещение тематического парка The Land of Legends.',
 'Турция', 'Белек', 1, 89900.00, '2026-06-15', '2026-06-22', 7,
 'Прямой перелет, Трансфер аэропорт-отель-аэропорт, Проживание Deluxe Room, Питание Ultra All Inclusive, Медстраховка, Посещение The Land of Legends',
 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80',
 TRUE, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'Королевский Дубай: Пальма Джумейра & Шопинг',
 'Премиум-тур в Дубай с размещением в Atlantis The Palm. Доступ в легендарный аквапарк, прогулки на яхте по Дубай Марине, экскурсия на смотровую площадку Бурдж-Халифа.',
 'ОАЭ', 'Дубай', 2, 145000.00, '2026-05-10', '2026-05-17', 7,
 'Авиаперелет Emirates, Индивидуальный трансфер, Номер Ocean King, Завтрак + Ужин (HB), Безлимитный вход в Aquaventure, Страховка',
 'https://images.unsplash.com/photo-1518684079-3c830dcef090?auto=format&fit=crop&w=1200&q=80',
 FALSE, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Тайны Красного моря: Хургада All Inclusive',
 'Отдых для всей семьи на песчаном побережье Хургады с уникальной морской лагуной. Идеальный сноркелинг, прогулки на катере и великолепное питание.',
 'Египет', 'Хургада', 3, 54900.00, '2026-05-20', '2026-05-27', 7,
 'Чартерный перелет, Групповой трансфер, Номер Superior с видом на сад, All Inclusive, Морская прогулка с масками, Страховка',
 'https://images.unsplash.com/photo-1539650116574-8efeb43e2750?auto=format&fit=crop&w=1200&q=80',
 TRUE, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, 'Тропическая гармония Бали: Джимбаран & Убуд',
 'Умиротворяющий отдых на острове богов. Закаты в Jimbaran Bay, йога-ретриты в Убуде, храм Улувату и знаменитый ужин из морепродуктов на побережье океана.',
 'Индонезия', 'Бали', 4, 118000.00, '2026-07-01', '2026-07-11', 10,
 'Регулярный перелет с пересадкой, Индивидуальный трансфер, Resort View Room, Завтраки шведский стол, Экскурсия в лес обезьян и храм Танах-Лот, Страховка',
 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80',
 FALSE, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(5, 'Альпийская сказка: Целль-ам-Зее и Капрун',
 'Очарование австрийских Альп: зеркальная гладь озера Целль, прогулки на теплоходе, подъем на ледник Китцштайнхорн (3029 м) и спа-релакс в термальном центре Tauern Spa.',
 'Австрия', 'Целль-ам-Зее', 5, 96000.00, '2026-06-05', '2026-06-12', 7,
 'Перелет в Зальцбург, Трансфер на комфортабельном шаттле, Полупансион (HB), Карточка Zell am See-Kaprun Card (бесплатные подъемники), Страховка',
 'https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=1200&q=80',
 FALSE, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(6, 'Горящий Стамбул: Мосты Босфора и Дворцы',
 'Экспресс-тур в город на стыке Европы и Азии: Айя-София, дворец Топкапы, Гранд-базар и вечерний круиз по проливу Босфор с ужином.',
 'Турция', 'Стамбул', 1, 38500.00, '2026-04-18', '2026-04-22', 4,
 'Прямой перелет, Трансфер, Отель 4* в историческом районе Султанахмет, Завтраки, Пешая экскурсия с гидом, Страховка',
 'https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?auto=format&fit=crop&w=1200&q=80',
 TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(7, 'Экзотический Пхукет: Пляж Карон & Острова Пхи-Пхи',
 'Тропический рай Андаманского моря: белый песок, кокосовые пальмы, тайский массаж и скоростная экскурсия на катере на остров Майя Бэй.',
 'Таиланд', 'Пхукет', 4, 76500.00, '2026-05-15', '2026-05-25', 10,
 'Авиаперелет, Трансфер, Deluxe Bungalow, Завтраки, Экскурсия на острова Пхи-Пхи с обедом, Страховка',
 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80',
 FALSE, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(8, 'Мальдивы: Вилла над бирюзовой лагуной',
 'Премиальный романтический побег на атолл Северный Мале. Снорклинг с мантами и черепахами, романтический ужин на пляже под звездами.',
 'Мальдивы', 'Мале', 2, 215000.00, '2026-08-10', '2026-08-18', 8,
 'Перелет, Трансфер на скоростном катере, Water Villa, All Inclusive Dine Around, Снаряжение для снорклинга, Медстраховка',
 'https://images.unsplash.com/photo-1514282401047-d79a71a590e8?auto=format&fit=crop&w=1200&q=80',
 TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(9, 'Каппадокия: Воздушные шары над долиной любви',
 'Уникальный уикенд среди скальных замков и пещерных городов. Полет на рассветном воздушном шаре над национальным парком Гёреме.',
 'Турция', 'Гёреме', 1, 46000.00, '2026-05-01', '2026-05-05', 4,
 'Перелет до Кайсери, Трансфер, Аутентичный Cave Hotel, Завтраки, Билет на воздушный шар, Фотосессия, Страховка',
 'https://images.unsplash.com/photo-1570939274717-7eda259b50ed?auto=format&fit=crop&w=1200&q=80',
 TRUE, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(10, 'Восточная сказка Абу-Даби: Лувр и мечеть шейха Зайда',
 'Культурное путешествие в столицу ОАЭ: беломраморная мечеть, футуристический Лувр Абу-Даби, парк Ferrari World и отдых на пляже Саадият.',
 'ОАЭ', 'Абу-Даби', 2, 105000.00, '2026-06-20', '2026-06-27', 7,
 'Прямой перелет Etihad Airways, Трансфер, Отель 5* на острове Саадият, Завтраки и ужины, Билеты в Лувр и Ferrari World',
 'https://images.unsplash.com/photo-1512632570417-a6097a049d2d?auto=format&fit=crop&w=1200&q=80',
 FALSE, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Seed Flights (8 flights)
INSERT INTO flights (id, flight_number, airline, departure_city, arrival_city, departure_time, arrival_time, price, available_seats, created_at) VALUES
(1, 'SU-2130', 'Аэрофлот', 'Москва', 'Анталья', '2026-06-15 08:30:00+03', '2026-06-15 13:45:00+03', 18500.00, 45, CURRENT_TIMESTAMP),
(2, 'SU-2131', 'Аэрофлот', 'Анталья', 'Москва', '2026-06-22 15:15:00+03', '2026-06-22 20:30:00+03', 19200.00, 48, CURRENT_TIMESTAMP),
(3, 'EK-132', 'Emirates', 'Москва', 'Дубай', '2026-05-10 11:20:00+03', '2026-05-10 17:55:04+04', 34000.00, 32, CURRENT_TIMESTAMP),
(4, 'EK-133', 'Emirates', 'Дубай', 'Москва', '2026-05-17 18:40:00+04', '2026-05-17 23:25:00+03', 35500.00, 28, CURRENT_TIMESTAMP),
(5, 'MS-729', 'EgyptAir', 'Москва', 'Хургада', '2026-05-20 06:15:00+03', '2026-05-20 12:00:00+02', 22400.00, 60, CURRENT_TIMESTAMP),
(6, 'QR-338', 'Qatar Airways', 'Москва', 'Бали', '2026-07-01 13:50:00+03', '2026-07-02 08:30:00+08', 49800.00, 20, CURRENT_TIMESTAMP),
(7, 'TK-414', 'Turkish Airlines', 'Москва', 'Стамбул', '2026-04-18 09:10:00+03', '2026-04-18 13:20:00+03', 16700.00, 40, CURRENT_TIMESTAMP),
(8, 'TG-975', 'Thai Airways', 'Москва', 'Пхукет', '2026-05-15 19:40:00+03', '2026-05-16 09:15:00+07', 41200.00, 25, CURRENT_TIMESTAMP);

-- 4. Seed Promo Codes
INSERT INTO promo_codes (id, code, discount_percent, min_order_amount, max_uses, current_uses, valid_until, active, created_at) VALUES
(1, 'WELCOME10', 10, 10000.00, 500, 0, '2027-01-01 00:00:00+00', TRUE, CURRENT_TIMESTAMP),
(2, 'SUMMER20', 20, 50000.00, 100, 0, '2026-09-01 00:00:00+00', TRUE, CURRENT_TIMESTAMP),
(3, 'NATIVE15', 15, 30000.00, 200, 0, '2026-12-31 23:59:59+00', TRUE, CURRENT_TIMESTAMP),
(4, 'FLY5', 5, 5000.00, 1000, 0, '2027-01-01 00:00:00+00', TRUE, CURRENT_TIMESTAMP);
