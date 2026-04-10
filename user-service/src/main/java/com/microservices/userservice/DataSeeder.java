package com.microservices.userservice;

import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("Users already exist, skipping seed.");
            return;
        }

        String hash = encoder.encode("password123");

        saveUser(
            "owner@test.com",
            hash,
            "OWNER",
            "Jean",
            "Dupont",
            "0612345678"
        );
        saveUser(
            "sophie.bernard@test.com",
            hash,
            "OWNER",
            "Sophie",
            "Bernard",
            "0623456789"
        );
        saveUser(
            "pierre.moreau@test.com",
            hash,
            "OWNER",
            "Pierre",
            "Moreau",
            "0634567890"
        );
        saveUser(
            "claire.petit@test.com",
            hash,
            "OWNER",
            "Claire",
            "Petit",
            "0645678901"
        );

        saveUser(
            "tenant@test.com",
            hash,
            "TENANT",
            "Marie",
            "Martin",
            "0698765432"
        );
        saveUser(
            "lucas.durand@test.com",
            hash,
            "TENANT",
            "Lucas",
            "Durand",
            "0687654321"
        );
        saveUser(
            "emma.leroy@test.com",
            hash,
            "TENANT",
            "Emma",
            "Leroy",
            "0676543210"
        );
        saveUser(
            "hugo.roux@test.com",
            hash,
            "TENANT",
            "Hugo",
            "Roux",
            "0665432109"
        );
        saveUser(
            "lea.david@test.com",
            hash,
            "TENANT",
            "Léa",
            "David",
            "0654321098"
        );
        saveUser(
            "thomas.bertrand@test.com",
            hash,
            "TENANT",
            "Thomas",
            "Bertrand",
            "0643210987"
        );
        saveUser(
            "camille.simon@test.com",
            hash,
            "TENANT",
            "Camille",
            "Simon",
            "0632109876"
        );
        saveUser(
            "nathan.michel@test.com",
            hash,
            "TENANT",
            "Nathan",
            "Michel",
            "0621098765"
        );
        saveUser(
            "julie.garcia@test.com",
            hash,
            "TENANT",
            "Julie",
            "Garcia",
            "0610987654"
        );
        saveUser(
            "antoine.martinez@test.com",
            hash,
            "TENANT",
            "Antoine",
            "Martinez",
            "0609876543"
        );

        System.out.println(
            "Seeded 14 users (4 owners, 10 tenants). Password: password123"
        );
    }

    private void saveUser(
        String email,
        String hash,
        String role,
        String first,
        String last,
        String phone
    ) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setRole(role);
        u.setFirstName(first);
        u.setLastName(last);
        u.setPhone(phone);
        userRepository.save(u);
    }
}
