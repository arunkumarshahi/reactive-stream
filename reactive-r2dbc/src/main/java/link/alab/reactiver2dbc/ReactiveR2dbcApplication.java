package link.alab.reactiver2dbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReactiveR2dbcApplication {

	@Bean
	ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory cf) {
		return r2dbcTransactionManager(cf);
	}

	@Bean
	TransactionalOperator transcationOperator(ReactiveTransactionManager rtm) {
		return TransactionalOperator.create(rtm);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(ReactiveR2dbcApplication.class, args);
	}
}

@Service
@RequiredArgsConstructor
class UserService {
	private final UserRepository reservationRepository;
	//private final TransactionalOperator transcationOperator;

	public Flux<User> saveReservation(String... nameVals) {
		Flux<String> names = Flux.just(nameVals);
		Flux<User> resrvations = names.map(name -> new User(null, name));
		// Flux<Mono<Reservation>>
		// savedReservation=resrvations.map(rsevation->reservationRepository.save(rsevation));
		// flattening publisher of publisher into publisher
		Flux<User> savedReservation = resrvations.flatMap(reservationRepository::save);
//		this.reservationRepository.deleteAll().thenMany(savedReservation).thenMany(reservationRepository.findAll())
//				.subscribe(System.out::println);
		//return this.transcationOperator.transactional(savedReservation);
		return savedReservation;
	}
}

@Data
@AllArgsConstructor
@Table("USER")
class User {
	@Id
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
}

interface UserRepository extends ReactiveCrudRepository<User, Long> {

}

@RequiredArgsConstructor
@Component
@Data
class DataLoader implements CommandLineRunner {
	@Autowired
	private UserRepository userRepository;
private final UserService userService;
	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
	
		Flux<User> savedUsers = userService.saveReservation("arun", "julie", "sanvi", "Shravya");

		this.userRepository.deleteAll().thenMany(savedUsers).thenMany(userRepository.findAll())
				.subscribe(System.out::println);

	}

}

@Configuration
//@EnableR2dbcRepositories(basePackages = "link.alab.reactiver2dbc")
class DBConfig {
//extends AbstractR2dbcConfiguration{
//	@Bean
//@Profile("test")
//	public ConnectionFactory connectionFactory() {
//		System.out.println(">>>>>>>>>> Using H2 in mem R2DBC connection factory");
//		 return new H2ConnectionFactory(
//	                H2ConnectionConfiguration.builder()
//	                        .url("mem:testdb;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4")
//	                        .username("sa")
//	                        .build());
//	}
	@Bean
	public ConnectionFactoryInitializer databaseInitializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);

		CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
		populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
		// populator.addPopulators(new ResourceDatabasePopulator(new
		// ClassPathResource("schema/data.sql")));
		initializer.setDatabasePopulator(populator);

		return initializer;
	}
}
