package link.alab.demoreactivedataapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Slf4j
public class DemoReactivedataAppApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(DemoReactivedataAppApplication.class, args);
	}

}

@Service
@RequiredArgsConstructor
class ReservationService {
	private final ReservationRepository reservationRepository;
	
	public Flux<Reservation> saveReservation(String... nameVals) {
		Flux<String> names = Flux.just(nameVals);
		Flux<Reservation> resrvations = names.map(name -> new Reservation(null, name));
		// Flux<Mono<Reservation>>
		// savedReservation=resrvations.map(rsevation->reservationRepository.save(rsevation));
		// flattening publisher of publisher into publisher
		Flux<Reservation> savedReservation = resrvations.flatMap(reservationRepository::save);
//		this.reservationRepository.deleteAll().thenMany(savedReservation).thenMany(reservationRepository.findAll())
//				.subscribe(System.out::println);
		return savedReservation;
	}
}

@Document
@AllArgsConstructor
@Data
@Slf4j
@Profile("mongo-db")
class Reservation {
	@Id
	private String id;
	private String name;
}

@Repository
interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {
	// Flux<Reservation> findByName(String name);
}

@Component
@RequiredArgsConstructor
@Slf4j
class SampleDataIntilizer implements CommandLineRunner {
	private final ReservationService reservationService;
	private final ReservationRepository reservationRepository;
	@Override
	public void run(String... args) throws Exception {
		log.info("loader is intilized");
		Flux<Reservation> savedReservation=reservationService.saveReservation("arun", "julie", "sanvi");
		
		this.reservationRepository.deleteAll().thenMany(savedReservation).thenMany(reservationRepository.findAll())
		.subscribe(System.out::println);
	}
}
