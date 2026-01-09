-- ============================================
-- Przykładowa baza danych do testów
-- Rental Tool Backend
-- ============================================

-- Czyszczenie istniejących danych w odpowiedniej kolejności (od zależnych do głównych)
DELETE FROM tool_images WHERE 1=1;
DELETE FROM reservations WHERE 1=1;
DELETE FROM tools WHERE 1=1;
DELETE FROM terms WHERE 1=1;
DELETE FROM email_verification_tokens WHERE 1=1;
DELETE FROM users WHERE 1=1;

-- ============================================
-- UŻYTKOWNICY
-- ============================================
-- Hasła: wszystkie używają "password123"
-- BCrypt hash dla "password123": $2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu

-- Wstawianie użytkowników
INSERT INTO users (id, first_name, last_name, email, password, phone_number, blocked, verified, user_type, created_at, updated_at, changed_password_at, blocked_at, verified_at) VALUES
-- Zwykli użytkownicy
(1, 'Jan', 'Kowalski', 'jan.kowalski@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 123 456 789', false, true, 'USER', '2025-01-01 10:00:00', '2025-01-01 10:00:00', NULL, NULL, '2025-01-01 10:00:00'),
(2, 'Anna', 'Nowak', 'anna.nowak@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 234 567 890', false, true, 'USER', '2025-01-02 11:00:00', '2025-01-02 11:00:00', NULL, NULL, '2025-01-02 11:00:00'),
(3, 'Piotr', 'Wiśniewski', 'piotr.wisniewski@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 345 678 901', false, true, 'USER', '2025-01-03 12:00:00', '2025-01-03 12:00:00', NULL, NULL, '2025-01-03 12:00:00'),
(4, 'Maria', 'Wójcik', 'maria.wojcik@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 456 789 012', false, true, 'USER', '2025-01-04 13:00:00', '2025-01-04 13:00:00', NULL, NULL, '2025-01-04 13:00:00'),
(5, 'Tomasz', 'Kowalczyk', 'tomasz.kowalczyk@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 567 890 123', false, true, 'USER', '2025-01-05 14:00:00', '2025-01-05 14:00:00', NULL, NULL, '2025-01-05 14:00:00'),
-- Moderator
(6, 'Moderator', 'Testowy', 'moderator@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 678 901 234', false, true, 'MODERATOR', '2025-01-06 15:00:00', '2025-01-06 15:00:00', NULL, NULL, '2025-01-06 15:00:00'),
-- Admin
(7, 'Admin', 'Systemowy', 'admin@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', '+48 789 012 345', false, true, 'ADMIN', '2025-01-07 16:00:00', '2025-01-07 16:00:00', NULL, NULL, '2025-01-07 16:00:00'),
-- Zablokowany użytkownik (do testów)
(8, 'Zablokowany', 'Użytkownik', 'blocked@example.com', '$2a$10$Xg6FVKz4Fs9KU2qzL5qUouFVX8VNHN91OtQxcjxVLCajwIwQerGBu', NULL, true, true, 'USER', '2025-01-08 17:00:00', '2025-01-08 17:00:00', NULL, '2025-01-08 17:30:00', '2025-01-08 17:00:00');

-- ============================================
-- REGULAMINY (TERMS) - muszą być przed TOOLS
-- ============================================

INSERT INTO terms (id, category, title, content, created_at, updated_at) VALUES
-- Regulamin ogólny (dla wszystkich kategorii)
(1, NULL, 'Regulamin ogólny wypożyczania narzędzi',
'REGULAMIN WYPOŻYCZANIA NARZĘDZI W LOKALNEJ SPOŁECZNOŚCI

1. ZASADY OGÓLNE
   - Aplikacja służy do łączenia członków lokalnej społeczności w celu wypożyczania narzędzi.
   - Wszystkie transakcje odbywają się poza aplikacją - użytkownicy umawiają się na spotkanie i dokonują transakcji bezpośrednio.
   - Aplikacja nie jest odpowiedzialna za przebieg transakcji ani za ewentualne szkody.

2. SPOTKANIE I TRANSAKCJA
   - Obie strony (właściciel i najemca) decydują się na dokonanie transakcji poza aplikacją.
   - Użytkownicy umawiają się na spotkanie w dogodnym dla obu stron miejscu i czasie.
   - Transakcja finansowa odbywa się bezpośrednio między stronami - aplikacja nie pośredniczy w płatnościach.
   - Zalecamy spotkanie w miejscu publicznym lub w obecności świadków.

3. ODPOWIEDZIALNOŚĆ ZA NARZĘDZIE
   - Najemca jest odpowiedzialny za narzędzie od momentu odbioru do momentu zwrotu.
   - Narzędzie powinno być zwrócone w takim samym stanie, w jakim zostało wypożyczone.
   - W przypadku zniszczenia, uszkodzenia lub utraty narzędzia, najemca zobowiązuje się do pokrycia kosztów naprawy lub wymiany.
   - Właściciel ma prawo do żądania rekompensaty za szkody powstałe w wyniku niewłaściwego użytkowania.

4. ZWROT NARZĘDZIA
   - Narzędzie należy zwrócić w terminie określonym w rezerwacji.
   - Przed zwrotem narzędzie powinno być wyczyszczone i przygotowane do użycia.
   - W przypadku opóźnienia w zwrocie, strony mogą ustalić dodatkowe opłaty.

5. ROZWIĄZYWANIE SPORÓW
   - W przypadku sporów, strony powinny najpierw spróbować rozwiązać problem polubownie.
   - W razie potrzeby, można skontaktować się z moderatorem aplikacji.
   - Aplikacja nie ponosi odpowiedzialności za spory między użytkownikami.

6. ZGODA
   - Akceptując ten regulamin, obie strony potwierdzają, że zapoznały się z jego treścią i akceptują wszystkie warunki.
   - Regulamin jest wiążący dla obu stron transakcji.',
'2025-01-01 10:00:00', '2025-01-01 10:00:00'),

-- Regulamin dla kategorii GARDENING
(2, 'GARDENING', 'Regulamin wypożyczania narzędzi ogrodniczych',
'REGULAMIN WYPOŻYCZANIA NARZĘDZI OGRODNICZYCH

1. ZASADY OGÓLNE
   - Regulamin dotyczy wypożyczania narzędzi ogrodniczych (kosiarki, sekatory, szpadle, grabie itp.).
   - Wszystkie transakcje odbywają się poza aplikacją - użytkownicy umawiają się na spotkanie.

2. SPOTKANIE I TRANSAKCJA
   - Obie strony decydują się na dokonanie transakcji poza aplikacją.
   - Użytkownicy umawiają się na spotkanie w dogodnym miejscu.
   - Transakcja finansowa odbywa się bezpośrednio między stronami.

3. ODPOWIEDZIALNOŚĆ ZA NARZĘDZIE
   - Najemca jest odpowiedzialny za narzędzie od momentu odbioru do momentu zwrotu.
   - Narzędzia ogrodnicze powinny być zwrócone czyste i suche.
   - W przypadku zniszczenia lub uszkodzenia (np. uszkodzenie ostrza kosiarki, złamanie trzonka szpadla), najemca zobowiązuje się do pokrycia kosztów naprawy lub wymiany.
   - Właściciel ma prawo do żądania rekompensaty za szkody powstałe w wyniku niewłaściwego użytkowania lub zaniedbania.

4. BEZPIECZEŃSTWO
   - Narzędzia ogrodnicze mogą być niebezpieczne - użytkownik zobowiązuje się do zachowania ostrożności.
   - Właściciel powinien poinformować najemcę o zasadach bezpiecznego użytkowania narzędzia.
   - Najemca ponosi pełną odpowiedzialność za szkody wyrządzone sobie lub innym podczas użytkowania narzędzia.

5. ZWROT NARZĘDZIA
   - Narzędzie należy zwrócić w terminie określonym w rezerwacji.
   - Przed zwrotem narzędzie powinno być wyczyszczone z ziemi, trawy i innych zanieczyszczeń.
   - Narzędzia z silnikami (kosiarki, pilarki) powinny być zwrócone z pustym zbiornikiem paliwa.

6. ZGODA
   - Akceptując ten regulamin, obie strony potwierdzają, że zapoznały się z jego treścią i akceptują wszystkie warunki.',
'2025-01-01 10:00:00', '2025-01-01 10:00:00'),

-- Regulamin dla kategorii CONSTRUCTION
(3, 'CONSTRUCTION', 'Regulamin wypożyczania narzędzi budowlanych',
'REGULAMIN WYPOŻYCZANIA NARZĘDZI BUDOWLANYCH

1. ZASADY OGÓLNE
   - Regulamin dotyczy wypożyczania narzędzi budowlanych (wiertarki, młoty, szlifierki, piły itp.).
   - Wszystkie transakcje odbywają się poza aplikacją - użytkownicy umawiają się na spotkanie.

2. SPOTKANIE I TRANSAKCJA
   - Obie strony decydują się na dokonanie transakcji poza aplikacją.
   - Użytkownicy umawiają się na spotkanie w dogodnym miejscu.
   - Transakcja finansowa odbywa się bezpośrednio między stronami.

3. ODPOWIEDZIALNOŚĆ ZA NARZĘDZIE
   - Najemca jest odpowiedzialny za narzędzie od momentu odbioru do momentu zwrotu.
   - Narzędzia budowlane powinny być zwrócone w stanie umożliwiającym dalsze użytkowanie.
   - W przypadku zniszczenia lub uszkodzenia (np. uszkodzenie wiertła, pęknięcie obudowy, uszkodzenie silnika), najemca zobowiązuje się do pokrycia kosztów naprawy lub wymiany.
   - Właściciel ma prawo do żądania rekompensaty za szkody powstałe w wyniku niewłaściwego użytkowania, przeciążenia lub zaniedbania.

4. BEZPIECZEŃSTWO
   - Narzędzia budowlane mogą być niebezpieczne - użytkownik zobowiązuje się do zachowania ostrożności i przestrzegania zasad BHP.
   - Właściciel powinien poinformować najemcę o zasadach bezpiecznego użytkowania narzędzia.
   - Najemca ponosi pełną odpowiedzialność za szkody wyrządzone sobie lub innym podczas użytkowania narzędzia.
   - Zalecane jest używanie odpowiednich środków ochrony osobistej (okulary, rękawice, ochrona słuchu).

5. ZWROT NARZĘDZIA
   - Narzędzie należy zwrócić w terminie określonym w rezerwacji.
   - Przed zwrotem narzędzie powinno być wyczyszczone z pyłu, brudu i innych zanieczyszczeń.
   - Narzędzia elektryczne powinny być zwrócone z kompletnym osprzętem (kable, akcesoria).

6. ZGODA
   - Akceptując ten regulamin, obie strony potwierdzają, że zapoznały się z jego treścią i akceptują wszystkie warunki.',
'2025-01-01 10:00:00', '2025-01-01 10:00:00'),

-- Regulamin dla kategorii ELECTRIC
(4, 'ELECTRIC', 'Regulamin wypożyczania narzędzi elektrycznych',
'REGULAMIN WYPOŻYCZANIA NARZĘDZI ELEKTRYCZNYCH

1. ZASADY OGÓLNE
   - Regulamin dotyczy wypożyczania narzędzi elektrycznych (wkrętarki, szlifierki, wiertarki, piły elektryczne itp.).
   - Wszystkie transakcje odbywają się poza aplikacją - użytkownicy umawiają się na spotkanie.

2. SPOTKANIE I TRANSAKCJA
   - Obie strony decydują się na dokonanie transakcji poza aplikacją.
   - Użytkownicy umawiają się na spotkanie w dogodnym miejscu.
   - Transakcja finansowa odbywa się bezpośrednio między stronami.

3. ODPOWIEDZIALNOŚĆ ZA NARZĘDZIE
   - Najemca jest odpowiedzialny za narzędzie od momentu odbioru do momentu zwrotu.
   - Narzędzia elektryczne powinny być zwrócone w stanie umożliwiającym dalsze użytkowanie.
   - W przypadku zniszczenia lub uszkodzenia (np. uszkodzenie kabla, uszkodzenie silnika, uszkodzenie obudowy), najemca zobowiązuje się do pokrycia kosztów naprawy lub wymiany.
   - Właściciel ma prawo do żądania rekompensaty za szkody powstałe w wyniku niewłaściwego użytkowania, przeciążenia lub zaniedbania.

4. BEZPIECZEŃSTWO
   - Narzędzia elektryczne mogą być niebezpieczne - użytkownik zobowiązuje się do zachowania ostrożności i przestrzegania zasad bezpieczeństwa.
   - Właściciel powinien poinformować najemcę o zasadach bezpiecznego użytkowania narzędzia.
   - Najemca ponosi pełną odpowiedzialność za szkody wyrządzone sobie lub innym podczas użytkowania narzędzia.
   - Narzędzie powinno być używane zgodnie z instrukcją producenta.

5. ZWROT NARZĘDZIA
   - Narzędzie należy zwrócić w terminie określonym w rezerwacji.
   - Przed zwrotem narzędzie powinno być wyczyszczone i przygotowane do użycia.
   - Narzędzie powinno być zwrócone z kompletnym osprzętem (kable, akcesoria, baterie jeśli dotyczy).

6. ZGODA
   - Akceptując ten regulamin, obie strony potwierdzają, że zapoznały się z jego treścią i akceptują wszystkie warunki.',
'2025-01-01 10:00:00', '2025-01-01 10:00:00'),

-- Regulamin dla kategorii PLUMBING
(5, 'PLUMBING', 'Regulamin wypożyczania narzędzi hydraulicznych',
'REGULAMIN WYPOŻYCZANIA NARZĘDZI HYDRAULICZNYCH

1. ZASADY OGÓLNE
   - Regulamin dotyczy wypożyczania narzędzi hydraulicznych (klucze, prasy, narzędzia do rur itp.).
   - Wszystkie transakcje odbywają się poza aplikacją - użytkownicy umawiają się na spotkanie.

2. SPOTKANIE I TRANSAKCJA
   - Obie strony decydują się na dokonanie transakcji poza aplikacją.
   - Użytkownicy umawiają się na spotkanie w dogodnym miejscu.
   - Transakcja finansowa odbywa się bezpośrednio między stronami.

3. ODPOWIEDZIALNOŚĆ ZA NARZĘDZIE
   - Najemca jest odpowiedzialny za narzędzie od momentu odbioru do momentu zwrotu.
   - Narzędzia hydrauliczne powinny być zwrócone czyste i suche.
   - W przypadku zniszczenia lub uszkodzenia (np. uszkodzenie klucza, uszkodzenie prasy), najemca zobowiązuje się do pokrycia kosztów naprawy lub wymiany.
   - Właściciel ma prawo do żądania rekompensaty za szkody powstałe w wyniku niewłaściwego użytkowania.

4. BEZPIECZEŃSTWO
   - Narzędzia hydrauliczne mogą być niebezpieczne - użytkownik zobowiązuje się do zachowania ostrożności.
   - Właściciel powinien poinformować najemcę o zasadach bezpiecznego użytkowania narzędzia.
   - Najemca ponosi pełną odpowiedzialność za szkody wyrządzone sobie lub innym podczas użytkowania narzędzia.

5. ZWROT NARZĘDZIA
   - Narzędzie należy zwrócić w terminie określonym w rezerwacji.
   - Przed zwrotem narzędzie powinno być wyczyszczone z wody, brudu i innych zanieczyszczeń.
   - Narzędzie powinno być zwrócone z kompletnym osprzętem (akcesoria, klucze).

6. ZGODA
   - Akceptując ten regulamin, obie strony potwierdzają, że zapoznały się z jego treścią i akceptują wszystkie warunki.',
'2025-01-01 10:00:00', '2025-01-01 10:00:00');

-- ============================================
-- NARZĘDZIA
-- ============================================

INSERT INTO tools (id, name, description, price_per_day, category, owner_id, address, latitude, longitude, main_image_url, terms_id, created_at, updated_at, is_active, moderation_status, moderator_id, moderated_at, moderation_comment) VALUES
-- Narzędzia w promieniu ~10km od punktu bazowego (50.0307635, 22.015645)
(1, 'Wiertarka udarowa Bosch', 'Profesjonalna wiertarka udarowa z zestawem wierteł. Idealna do prac budowlanych.', 50.00, 'CONSTRUCTION', 1, 'Rzeszów - centrum', 50.0412, 21.9991, NULL, 3, '2025-01-10 09:00:00', '2025-01-10 09:00:00', true, 'APPROVED', 6, '2025-01-10 10:00:00', 'Narzędzie zatwierdzone'),
(2, 'Kosiarka spalinowa', 'Kosiarka spalinowa 5.5KM, szerokość koszenia 46cm. Idealna do dużych trawników.', 80.00, 'GARDENING', 1, 'Rzeszów - Staroniwa', 50.0200, 22.0300, NULL, 2, '2025-01-11 10:00:00', '2025-01-11 10:00:00', true, 'APPROVED', 6, '2025-01-11 11:00:00', 'Zatwierdzone'),
(3, 'Wkrętarka akumulatorowa', 'Wkrętarka akumulatorowa 18V z dwoma bateriami. Kompletna w zestawie.', 40.00, 'ELECTRIC', 1, 'Rzeszów - Drabinianka', 50.0150, 22.0050, NULL, 4, '2025-01-12 11:00:00', '2025-01-12 11:00:00', true, 'APPROVED', 6, '2025-01-12 12:00:00', 'OK'),

-- Narzędzia w promieniu ~25km od punktu bazowego
(4, 'Młot pneumatyczny', 'Młot pneumatyczny do kucia betonu i asfaltu. Bardzo wydajny.', 120.00, 'CONSTRUCTION', 2, 'Głogów Małopolski', 50.1350, 21.9700, NULL, 3, '2025-01-13 12:00:00', '2025-01-13 12:00:00', true, 'APPROVED', 6, '2025-01-13 13:00:00', 'Zatwierdzone'),
(5, 'Sekator elektryczny', 'Sekator elektryczny do żywopłotów. Długość ostrza 60cm.', 35.00, 'GARDENING', 2, 'Tyczyn', 50.1050, 22.0300, NULL, 2, '2025-01-14 13:00:00', '2025-01-14 13:00:00', true, 'APPROVED', 6, '2025-01-14 14:00:00', 'OK'),
(6, 'Wiertarka do betonu', 'Wiertarka udarowa do betonu 1500W. Zestaw wierteł w zestawie.', 60.00, 'CONSTRUCTION', 2, 'Boguchwała', 50.0850, 22.1400, NULL, 3, '2025-01-15 14:00:00', '2025-01-15 14:00:00', true, 'APPROVED', 6, '2025-01-15 15:00:00', 'Zatwierdzone'),

-- Narzędzia w promieniu ~50km od punktu bazowego
(7, 'Klucz nasadowy', 'Komplet kluczy nasadowych 1/2 cala, 72 sztuki. Profesjonalny zestaw.', 45.00, 'OTHER', 3, 'Łańcut', 50.0670, 22.2290, NULL, 1, '2025-01-16 15:00:00', '2025-01-16 15:00:00', true, 'APPROVED', 6, '2025-01-16 16:00:00', 'Zatwierdzone'),
(8, 'Prasa do rur', 'Prasa hydrauliczna do rur miedzianych i z tworzyw sztucznych.', 90.00, 'PLUMBING', 3, 'Leżajsk', 50.2580, 22.4190, NULL, 5, '2025-01-17 16:00:00', '2025-01-17 16:00:00', true, 'APPROVED', 6, '2025-01-17 17:00:00', 'OK'),
(9, 'Pilarka tarczowa', 'Pilarka tarczowa 2000W do drewna i metalu. Nowa, nieużywana.', 55.00, 'ELECTRIC', 4, 'Przeworsk', 50.0590, 22.4940, NULL, 4, '2025-01-18 17:00:00', '2025-01-18 17:00:00', true, 'PENDING', NULL, NULL, NULL),

-- Narzędzia w promieniu ~100km od punktu bazowego
(10, 'Wąż ogrodowy', 'Wąż ogrodowy 50m z pistoletem zraszającym. Wysokiej jakości.', 25.00, 'GARDENING', 4, 'Jarosław', 50.0170, 22.6770, NULL, 2, '2025-01-19 18:00:00', '2025-01-19 18:00:00', true, 'PENDING', NULL, NULL, NULL),
(11, 'Narzędzie testowe', 'To narzędzie zostało odrzucone przez moderatora.', 30.00, 'OTHER', 5, 'Stalowa Wola', 50.5680, 22.0530, NULL, 1, '2025-01-20 19:00:00', '2025-01-20 19:00:00', false, 'REJECTED', 6, '2025-01-20 20:00:00', 'Narzędzie nie spełnia wymagań'),
(12, 'Szpadel', 'Szpadel ogrodowy z trzonkiem drewnianym. Używany ale sprawny.', 15.00, 'GARDENING', 1, 'Tarnobrzeg', 50.5730, 21.6780, NULL, 2, '2025-01-21 20:00:00', '2025-01-21 20:00:00', false, 'APPROVED', 6, '2025-01-21 21:00:00', 'Zatwierdzone');

-- ============================================
-- REZERWACJE
-- ============================================

INSERT INTO reservations (id, tool_id, renter_id, start_date, end_date, total_price, status, terms_id, terms_accepted_at, created_at, updated_at) VALUES
-- Rezerwacja oczekująca (użytkownik 2 wynajmuje od użytkownika 1)
(1, 1, 2, '2025-12-01', '2025-12-05', 200.00, 'PENDING', NULL, NULL, '2025-11-20 10:00:00', '2025-11-20 10:00:00'),

-- Rezerwacja potwierdzona (użytkownik 3 wynajmuje od użytkownika 1)
(2, 2, 3, '2025-12-10', '2025-12-15', 400.00, 'CONFIRMED', NULL, NULL, '2025-11-21 11:00:00', '2025-11-21 12:00:00'),

-- Rezerwacja z zaakceptowanym regulaminem (użytkownik 4 wynajmuje od użytkownika 2)
(3, 4, 4, '2025-12-20', '2025-12-25', 600.00, 'REGULATIONS_ACCEPTED', 3, '2025-11-22 13:00:00', '2025-11-22 12:00:00', '2025-11-22 13:00:00'),

-- Rezerwacja anulowana (użytkownik 4 wynajmuje od użytkownika 1)
(4, 3, 4, '2025-12-01', '2025-12-03', 80.00, 'CANCELED', NULL, NULL, '2025-11-23 14:00:00', '2025-11-23 15:00:00'),

-- Rezerwacja potwierdzona (użytkownik 2 wynajmuje od użytkownika 3)
(5, 7, 2, '2025-12-15', '2025-12-20', 225.00, 'CONFIRMED', NULL, NULL, '2025-11-24 15:00:00', '2025-11-24 16:00:00');

-- ============================================
-- OBRAZY NARZĘDZI
-- ============================================

INSERT INTO tool_images (id, url, filename, content_type, is_main, tool_id, created_at, updated_at) VALUES
-- Narzędzie 1: Wiertarka udarowa Bosch
(1, 'http://localhost:8080/api/v1/files/tool1_main.jpg', 'tool1_main.jpg', 'image/jpeg', true, 1, '2025-01-10 09:30:00', '2025-01-10 09:30:00'),
(2, 'http://localhost:8080/api/v1/files/tool1_2.jpg', 'tool1_2.jpg', 'image/jpeg', false, 1, '2025-01-10 09:31:00', '2025-01-10 09:31:00'),
(3, 'http://localhost:8080/api/v1/files/tool1_3.jpg', 'tool1_3.jpg', 'image/jpeg', false, 1, '2025-01-10 09:32:00', '2025-01-10 09:32:00'),

-- Narzędzie 2: Kosiarka spalinowa
(4, 'http://localhost:8080/api/v1/files/tool2_main.jpg', 'tool2_main.jpg', 'image/jpeg', true, 2, '2025-01-11 10:30:00', '2025-01-11 10:30:00'),
(5, 'http://localhost:8080/api/v1/files/tool2_2.jpg', 'tool2_2.jpg', 'image/jpeg', false, 2, '2025-01-11 10:31:00', '2025-01-11 10:31:00'),

-- Narzędzie 3: Wkrętarka akumulatorowa
(6, 'http://localhost:8080/api/v1/files/tool3_main.jpg', 'tool3_main.jpg', 'image/jpeg', true, 3, '2025-01-12 11:30:00', '2025-01-12 11:30:00'),
(7, 'http://localhost:8080/api/v1/files/tool3_2.jpg', 'tool3_2.jpg', 'image/jpeg', false, 3, '2025-01-12 11:31:00', '2025-01-12 11:31:00'),

-- Narzędzie 4: Młot pneumatyczny
(8, 'http://localhost:8080/api/v1/files/tool4_main.jpg', 'tool4_main.jpg', 'image/jpeg', true, 4, '2025-01-13 12:30:00', '2025-01-13 12:30:00'),
(9, 'http://localhost:8080/api/v1/files/tool4_2.jpg', 'tool4_2.jpg', 'image/jpeg', false, 4, '2025-01-13 12:31:00', '2025-01-13 12:31:00'),

-- Narzędzie 5: Sekator elektryczny
(10, 'http://localhost:8080/api/v1/files/tool5_main.jpg', 'tool5_main.jpg', 'image/jpeg', true, 5, '2025-01-14 13:30:00', '2025-01-14 13:30:00'),

-- Narzędzie 6: Wiertarka do betonu
(11, 'http://localhost:8080/api/v1/files/tool6_main.jpg', 'tool6_main.jpg', 'image/jpeg', true, 6, '2025-01-15 14:30:00', '2025-01-15 14:30:00'),
(12, 'http://localhost:8080/api/v1/files/tool6_2.jpg', 'tool6_2.jpg', 'image/jpeg', false, 6, '2025-01-15 14:31:00', '2025-01-15 14:31:00'),

-- Narzędzie 7: Klucz nasadowy
(13, 'http://localhost:8080/api/v1/files/tool7_main.jpg', 'tool7_main.jpg', 'image/jpeg', true, 7, '2025-01-16 15:30:00', '2025-01-16 15:30:00'),

-- Narzędzie 8: Prasa do rur
(14, 'http://localhost:8080/api/v1/files/tool8_main.jpg', 'tool8_main.jpg', 'image/jpeg', true, 8, '2025-01-17 16:30:00', '2025-01-17 16:30:00'),

-- Narzędzie 9: Pilarka tarczowa (PENDING)
(15, 'http://localhost:8080/api/v1/files/tool9_main.jpg', 'tool9_main.jpg', 'image/jpeg', true, 9, '2025-01-18 17:30:00', '2025-01-18 17:30:00'),
(16, 'http://localhost:8080/api/v1/files/tool9_2.jpg', 'tool9_2.jpg', 'image/jpeg', false, 9, '2025-01-18 17:31:00', '2025-01-18 17:31:00'),

-- Narzędzie 10: Wąż ogrodowy (PENDING)
(17, 'http://localhost:8080/api/v1/files/tool10_main.jpg', 'tool10_main.jpg', 'image/jpeg', true, 10, '2025-01-19 18:30:00', '2025-01-19 18:30:00'),

-- Narzędzie 11: Narzędzie testowe (REJECTED)
(18, 'http://localhost:8080/api/v1/files/tool11_main.jpg', 'tool11_main.jpg', 'image/jpeg', true, 11, '2025-01-20 19:30:00', '2025-01-20 19:30:00'),

-- Narzędzie 12: Szpadel (nieaktywne)
(19, 'http://localhost:8080/api/v1/files/tool12_main.jpg', 'tool12_main.jpg', 'image/jpeg', true, 12, '2025-01-21 20:30:00', '2025-01-21 20:30:00');

-- ============================================
-- AKTUALIZACJA main_image_url DLA NARZĘDZI
-- ============================================

UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool1_main.jpg' WHERE id = 1;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool2_main.jpg' WHERE id = 2;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool3_main.jpg' WHERE id = 3;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool4_main.jpg' WHERE id = 4;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool5_main.jpg' WHERE id = 5;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool6_main.jpg' WHERE id = 6;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool7_main.jpg' WHERE id = 7;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool8_main.jpg' WHERE id = 8;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool9_main.jpg' WHERE id = 9;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool10_main.jpg' WHERE id = 10;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool11_main.jpg' WHERE id = 11;
UPDATE tools SET main_image_url = 'http://localhost:8080/api/v1/files/tool12_main.jpg' WHERE id = 12;

-- ============================================
-- PODSUMOWANIE DANYCH TESTOWYCH
-- ============================================
-- Użytkownicy: 8 (5 USER, 1 MODERATOR, 1 ADMIN, 1 zablokowany)
-- Narzędzia: 12 (8 zatwierdzonych aktywnych, 2 oczekujące, 1 odrzucone, 1 nieaktywne)
-- Rezerwacje: 5 (różne statusy)
-- Obrazy: 19 (każde narzędzie ma przynajmniej jedno główne zdjęcie)
-- Regulaminy: 5 (1 ogólny + 4 kategorialne)
--
-- Dane logowania (hasło dla wszystkich: password123):
-- - jan.kowalski@example.com (USER, właściciel narzędzi 1,2,3,12)
-- - anna.nowak@example.com (USER, właściciel narzędzi 4,5,6)
-- - piotr.wisniewski@example.com (USER, właściciel narzędzi 7,8,9)
-- - maria.wojcik@example.com (USER, właściciel narzędzi 10)
-- - tomasz.kowalczyk@example.com (USER, właściciel narzędzia 11)
-- - moderator@example.com (MODERATOR)
-- - admin@example.com (ADMIN)
-- - blocked@example.com (USER, zablokowany)
--
-- Lokalizacje narzędzi (od punktu 50.0307635, 22.015645):
-- ~10km: Narzędzia 1, 2, 3 (Rzeszów - centrum, Staroniwa, Drabinianka)
-- ~25km: Narzędzia 4, 5, 6 (Głogów Małopolski, Tyczyn, Boguchwała)
-- ~50km: Narzędzia 7, 8, 9 (Łańcut, Leżajsk, Przeworsk)
-- ~100km: Narzędzia 10, 11, 12 (Jarosław, Stalowa Wola, Tarnobrzeg)
