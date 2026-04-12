package com.microservices.propertyservice;

import com.microservices.propertyservice.model.Property;
import com.microservices.propertyservice.model.Reservation;
import com.microservices.propertyservice.repository.PropertyRepository;
import com.microservices.propertyservice.repository.ReservationRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    // Clés des photos pré-chargées dans MinIO par minio-init au démarrage
    private static final int PHOTO_COUNT = 20;
    private int photoIndex = 0;

    private final PropertyRepository propertyRepository;
    private final ReservationRepository reservationRepository;

    public DataSeeder(
        PropertyRepository propertyRepository,
        ReservationRepository reservationRepository
    ) {
        this.propertyRepository = propertyRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) {
        if (propertyRepository.count() > 0) {
            System.out.println("Data already exists, skipping seed.");
            return;
        }

        save(
            "Studio Belleville",
            "STUDIO",
            "Paris 20e",
            45,
            "Charmant studio de 22m2 au coeur de Belleville, proche metro Couronnes",
            1
        );
        save(
            "T2 Republique",
            "APARTMENT",
            "Paris 11e",
            85,
            "Appartement T2 renove 40m2, parquet, double vitrage, lumineux",
            1
        );
        save(
            "T3 Bastille",
            "APARTMENT",
            "Paris 4e",
            120,
            "Grand T3 de 65m2, 2 chambres, vue sur cour arboree",
            1
        );
        save(
            "Loft Oberkampf",
            "STUDIO",
            "Paris 11e",
            95,
            "Loft atypique 35m2, mezzanine, esprit industriel",
            1
        );
        save(
            "Studio Nation",
            "STUDIO",
            "Paris 12e",
            40,
            "Studio fonctionnel 18m2, cuisine equipee, metro Nation",
            1
        );
        save(
            "T2 Montmartre",
            "APARTMENT",
            "Paris 18e",
            78,
            "T2 avec vue sur Sacre-Coeur, 38m2, charme parisien",
            1
        );
        save(
            "T4 Familial Vincennes",
            "APARTMENT",
            "Vincennes",
            150,
            "Grand appartement familial 85m2, 3 chambres, proche bois",
            1
        );
        save(
            "Studio Marais",
            "STUDIO",
            "Paris 3e",
            65,
            "Studio cosy 20m2 dans le Marais, emplacement premium",
            1
        );
        save(
            "Maison Montreuil",
            "HOUSE",
            "Montreuil",
            110,
            "Maison 3 pieces avec jardin 30m2, quartier calme",
            1
        );
        save(
            "T2 Canal Saint-Martin",
            "APARTMENT",
            "Paris 10e",
            90,
            "T2 donnant sur le canal, 42m2, lumineux",
            1
        );
        save(
            "Studio Chatelet",
            "STUDIO",
            "Paris 1er",
            55,
            "Micro-studio 15m2 hyper centre, ideal court sejour",
            1
        );
        save(
            "T3 Buttes-Chaumont",
            "APARTMENT",
            "Paris 19e",
            105,
            "T3 familial 60m2, vue sur parc, calme et verdure",
            1
        );
        save(
            "Duplex Menilmontant",
            "APARTMENT",
            "Paris 20e",
            115,
            "Duplex original 55m2, terrasse 10m2, vue toits de Paris",
            1
        );
        save(
            "Studio Gare de Lyon",
            "STUDIO",
            "Paris 12e",
            50,
            "Studio meuble 22m2, ideal voyageurs, 2min gare",
            1
        );
        save(
            "T2 Batignolles",
            "APARTMENT",
            "Paris 17e",
            82,
            "T2 au calme 36m2, quartier village des Batignolles",
            1
        );

        save(
            "T3 Vieux-Port",
            "APARTMENT",
            "Marseille 1er",
            75,
            "Bel appartement T3 avec vue sur le Vieux-Port, lumineux, 65m2",
            2
        );
        save(
            "Villa Calanques",
            "HOUSE",
            "Marseille 8e",
            180,
            "Villa 4 pieces avec piscine, acces direct calanques",
            2
        );
        save(
            "Studio Canebiere",
            "STUDIO",
            "Marseille 1er",
            35,
            "Studio renove 20m2 sur la Canebiere, metro direct",
            2
        );
        save(
            "T2 Panier",
            "APARTMENT",
            "Marseille 2e",
            60,
            "T2 de charme dans le quartier du Panier, 45m2",
            2
        );
        save(
            "Loft Joliette",
            "STUDIO",
            "Marseille 2e",
            70,
            "Ancien entrepot converti, 50m2, style industriel",
            2
        );
        save(
            "T3 Prado",
            "APARTMENT",
            "Marseille 8e",
            95,
            "T3 standing 70m2, proche plages du Prado, parking",
            2
        );
        save(
            "Studio Castellane",
            "STUDIO",
            "Marseille 6e",
            38,
            "Studio etudiant 18m2, proche fac et metro",
            2
        );
        save(
            "Maison Endoume",
            "HOUSE",
            "Marseille 7e",
            140,
            "Maison de pecheur renovee, 3 chambres, vue mer",
            2
        );
        save(
            "T2 Longchamp",
            "APARTMENT",
            "Marseille 4e",
            55,
            "T2 haussmannien 40m2, hauteur sous plafond, parquet",
            2
        );
        save(
            "T4 Bonneveine",
            "APARTMENT",
            "Marseille 9e",
            120,
            "Grand T4 familial 80m2, balcon, proche plage",
            2
        );
        save(
            "Studio Cours Julien",
            "STUDIO",
            "Marseille 6e",
            42,
            "Studio artiste 25m2, quartier branche et vivant",
            2
        );
        save(
            "T2 Corniche",
            "APARTMENT",
            "Marseille 7e",
            88,
            "T2 avec terrasse vue mer, 45m2, coucher de soleil",
            2
        );
        save(
            "Maison Allauch",
            "HOUSE",
            "Allauch",
            100,
            "Maison provencale 4 pieces, jardin, collines",
            2
        );
        save(
            "Studio Timone",
            "STUDIO",
            "Marseille 5e",
            30,
            "Studio meuble 16m2, proche hopitaux et fac medecine",
            2
        );
        save(
            "T3 Pointe Rouge",
            "APARTMENT",
            "Marseille 8e",
            105,
            "T3 lumineux 65m2, a 200m de la plage",
            2
        );

        save(
            "Maison avec jardin",
            "HOUSE",
            "Lyon 5e",
            95,
            "Maison de ville 90m2 avec jardin privatif, 3 chambres, quartier calme",
            3
        );
        save(
            "T2 Presqu'ile",
            "APARTMENT",
            "Lyon 2e",
            72,
            "T2 central 38m2, entre Bellecour et Perrache",
            3
        );
        save(
            "Studio Part-Dieu",
            "STUDIO",
            "Lyon 3e",
            40,
            "Studio meuble 20m2, 5min gare Part-Dieu",
            3
        );
        save(
            "T3 Croix-Rousse",
            "APARTMENT",
            "Lyon 4e",
            88,
            "T3 avec vue sur Lyon, 60m2, esprit village",
            3
        );
        save(
            "Loft Confluence",
            "STUDIO",
            "Lyon 2e",
            110,
            "Loft moderne 55m2 dans le quartier Confluence",
            3
        );
        save(
            "T2 Vieux Lyon",
            "APARTMENT",
            "Lyon 5e",
            68,
            "T2 de caractere dans les traboules, 35m2, poutres",
            3
        );
        save(
            "Maison Caluire",
            "HOUSE",
            "Caluire",
            130,
            "Maison familiale 100m2, jardin 200m2, garage",
            3
        );
        save(
            "Studio Guillotiere",
            "STUDIO",
            "Lyon 7e",
            35,
            "Studio etudiant 17m2, quartier vivant, metro D",
            3
        );
        save(
            "T4 Tete d'Or",
            "APARTMENT",
            "Lyon 6e",
            155,
            "Grand T4 standing 90m2, face au parc Tete d'Or",
            3
        );
        save(
            "T2 Saxe-Gambetta",
            "APARTMENT",
            "Lyon 7e",
            62,
            "T2 renove 35m2, metro direct, quartier dynamique",
            3
        );

        save(
            "Studio Gambetta",
            "STUDIO",
            "Bordeaux Centre",
            38,
            "Studio meuble 18m2, ideal etudiant, proche tramway",
            4
        );
        save(
            "T2 Chartrons",
            "APARTMENT",
            "Bordeaux Chartrons",
            65,
            "T2 dans quartier tendance, 40m2, caves a vin a proximite",
            4
        );
        save(
            "Echoppe bordelaise",
            "HOUSE",
            "Bordeaux Bastide",
            95,
            "Echoppe typique 75m2, jardin, charme bordelais",
            4
        );
        save(
            "T3 Place de la Bourse",
            "APARTMENT",
            "Bordeaux Centre",
            110,
            "T3 de prestige 70m2, vue miroir d'eau",
            4
        );
        save(
            "Studio Saint-Michel",
            "STUDIO",
            "Bordeaux Centre",
            32,
            "Studio compact 15m2, hyper centre, vie etudiante",
            4
        );
        save(
            "T2 Meriadeck",
            "APARTMENT",
            "Bordeaux Centre",
            58,
            "T2 moderne 38m2, residence recente, tramway",
            4
        );
        save(
            "Maison Cauderan",
            "HOUSE",
            "Bordeaux Cauderan",
            120,
            "Maison familiale 85m2, jardin, quartier residentiel",
            4
        );
        save(
            "T2 Victoire",
            "APARTMENT",
            "Bordeaux Centre",
            55,
            "T2 etudiant 32m2, place de la Victoire, anime",
            4
        );
        save(
            "Studio Stalingrad",
            "STUDIO",
            "Bordeaux Bastide",
            35,
            "Studio renove 20m2, rive droite, calme",
            4
        );
        save(
            "T3 Jardin Public",
            "APARTMENT",
            "Bordeaux Centre",
            98,
            "T3 bourgeois 65m2, parquet, moulures, charme",
            4
        );

        System.out.println("Seeded 50 properties");

        LocalDate today = LocalDate.now();

        saveRes(1, 5, today.plusDays(5), today.plusDays(10), "CONFIRMED");
        saveRes(16, 5, today.plusDays(15), today.plusDays(20), "PENDING");
        saveRes(31, 5, today.minusDays(10), today.minusDays(5), "CONFIRMED");
        saveRes(41, 5, today.plusDays(30), today.plusDays(35), "CANCELLED");
        saveRes(
            8,
            5,
            today.plusDays(12),
            today.plusDays(14),
            "CANCELLATION_REQUESTED"
        );

        saveRes(2, 6, today.plusDays(3), today.plusDays(7), "CONFIRMED");
        saveRes(17, 6, today.plusDays(20), today.plusDays(25), "PENDING");
        saveRes(
            32,
            6,
            today.plusDays(8),
            today.plusDays(12),
            "CANCELLATION_REFUSED"
        );
        saveRes(42, 6, today.minusDays(20), today.minusDays(15), "CANCELLED");

        saveRes(3, 7, today.plusDays(1), today.plusDays(5), "CONFIRMED");
        saveRes(18, 7, today.plusDays(10), today.plusDays(15), "PENDING");
        saveRes(33, 7, today.plusDays(25), today.plusDays(30), "PENDING");
        saveRes(43, 7, today.plusDays(40), today.plusDays(45), "CONFIRMED");

        saveRes(4, 8, today.plusDays(7), today.plusDays(14), "PENDING");
        saveRes(19, 8, today.plusDays(2), today.plusDays(6), "CONFIRMED");
        saveRes(
            34,
            8,
            today.plusDays(20),
            today.plusDays(22),
            "CANCELLATION_REQUESTED"
        );

        saveRes(5, 9, today.plusDays(4), today.plusDays(8), "CONFIRMED");
        saveRes(20, 9, today.plusDays(15), today.plusDays(18), "PENDING");
        saveRes(44, 9, today.minusDays(5), today.minusDays(1), "CONFIRMED");

        saveRes(6, 10, today.plusDays(10), today.plusDays(15), "PENDING");
        saveRes(21, 10, today.plusDays(1), today.plusDays(3), "CONFIRMED");
        saveRes(35, 10, today.plusDays(20), today.plusDays(28), "CANCELLED");

        saveRes(7, 11, today.plusDays(6), today.plusDays(11), "CONFIRMED");
        saveRes(
            22,
            11,
            today.plusDays(18),
            today.plusDays(22),
            "CANCELLATION_REQUESTED"
        );

        saveRes(9, 12, today.plusDays(3), today.plusDays(9), "PENDING");
        saveRes(36, 12, today.plusDays(15), today.plusDays(20), "CONFIRMED");

        saveRes(10, 13, today.plusDays(5), today.plusDays(8), "CONFIRMED");
        saveRes(45, 13, today.plusDays(25), today.plusDays(30), "PENDING");

        saveRes(11, 14, today.plusDays(2), today.plusDays(5), "PENDING");
        saveRes(
            23,
            14,
            today.plusDays(12),
            today.plusDays(16),
            "CANCELLATION_REFUSED"
        );

        System.out.println("Seeded 30 reservations (various statuses)");
    }

    private void save(
        String title,
        String type,
        String location,
        double price,
        String description,
        long ownerId
    ) {
        Property p = new Property();
        p.setTitle(title);
        p.setType(type);
        p.setLocation(location);
        p.setPrice(price);
        p.setDescription(description);
        p.setOwnerId(ownerId);
        // Photo pré-chargée dans MinIO par minio-init (seed-photo-01 à seed-photo-20)
        String key = String.format("seed-photo-%02d", photoIndex++ % PHOTO_COUNT + 1);
        p.getPhotoKeys().add(key);
        propertyRepository.save(p);
    }

    // Tenant contacts indexed by tenantId (ids 5-14)
    private static final String[][] TENANTS = {
        {"Marie Martin", "tenant@test.com", "0698765432"},
        {"Lucas Durand", "lucas.durand@test.com", "0687654321"},
        {"Emma Leroy", "emma.leroy@test.com", "0676543210"},
        {"Hugo Roux", "hugo.roux@test.com", "0665432109"},
        {"Léa David", "lea.david@test.com", "0654321098"},
        {"Thomas Bertrand", "thomas.bertrand@test.com", "0643210987"},
        {"Camille Simon", "camille.simon@test.com", "0632109876"},
        {"Nathan Michel", "nathan.michel@test.com", "0621098765"},
        {"Julie Garcia", "julie.garcia@test.com", "0610987654"},
        {"Antoine Martinez", "antoine.martinez@test.com", "0609876543"},
    };

    private void saveRes(
        long propertyId,
        long tenantId,
        LocalDate start,
        LocalDate end,
        String status
    ) {
        Reservation r = new Reservation();
        r.setPropertyId(propertyId);
        r.setTenantId(tenantId);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setStatus(status);
        int idx = (int)(tenantId - 5);
        if (idx >= 0 && idx < TENANTS.length) {
            r.setTenantName(TENANTS[idx][0]);
            r.setTenantEmail(TENANTS[idx][1]);
            r.setTenantPhone(TENANTS[idx][2]);
        }
        reservationRepository.save(r);
    }
}
