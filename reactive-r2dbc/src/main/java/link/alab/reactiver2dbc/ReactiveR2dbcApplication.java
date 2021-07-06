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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import io.r2dbc.spi.ConnectionFactories;
//import io.r2dbc.h2.H2ConnectionConfiguration;
//import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Vector;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.MAX_SIZE;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@SpringBootApplication
public class ReactiveR2dbcApplication {

//	@Bean
//	ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory cf) {
//		return r2dbcTransactionManager(cf);
//	}
//
//	@Bean
//	TransactionalOperator transcationOperator(ReactiveTransactionManager rtm) {
//		return TransactionalOperator.create(rtm);
//	}
//	
	public static void main(String[] args) {
		SpringApplication.run(ReactiveR2dbcApplication.class, args);
	}
}

@RestController
class HelloController{
	@GetMapping("/eater/{id}")
	public String getEater(@PathVariable Integer id){
				int index =0;
					Vector v = new Vector();
		Runtime rt = Runtime.getRuntime();
					while (index<id)
					{
						index++;
						byte b[] = new byte[1000*1000];
						v.add(b);

						System.out.println( "free memory: " + rt.freeMemory() );
					}
					return "free memory: " + rt.freeMemory();
				}
}
@Service
@RequiredArgsConstructor
class UserService {
	private final UserRepository reservationRepository;

	// private final TransactionalOperator transcationOperator;
//	 @Transactional
	public Flux<User> saveReservation(String... nameVals) {
		Flux<String> names = Flux.just(nameVals);
		Flux<User> resrvations = names.map(name -> new User( null, name));
		// Flux<Mono<Reservation>>
		// savedReservation=resrvations.map(rsevation->reservationRepository.save(rsevation));
		// flattening publisher of publisher into publisher
		Flux<User> savedReservation = resrvations.flatMap(reservationRepository::save).doOnNext(this::assertValid);
//		this.reservationRepository.deleteAll().thenMany(savedReservation).thenMany(reservationRepository.findAll())
//				.subscribe(System.out::println);
//		 return this.transcationOperator.transactional(savedReservation);
		return savedReservation;
	}

	private void assertValid(User user) {
		Assert.isTrue(user.getName() != null && Character.isUpperCase(user.getName().charAt(0)),
				"The name must start with capital letter");
	}
}

@Data
@AllArgsConstructor
@Table("AUSER")
class User {
	@Id
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
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

		Flux<User> savedUsers = userService.saveReservation("XArun", "XJulie", "Sanvi", "XShravya");

		this.userRepository.deleteAll().thenMany(savedUsers).thenMany(userRepository.findAll())
				.subscribe(System.out::println);

	}

}

//@Configuration
//@EnableR2dbcRepositories
//class R2DBCConfig {
//
//	@Bean
//	public ConnectionFactory connectionFactory() {
//		return ConnectionFactories.get(ConnectionFactoryOptions.builder().option(DRIVER, "postgresql")
//				.option(HOST, "db").option(PORT, 5432).option(USER, "postgres").option(PASSWORD, "secret123")
//				.option(DATABASE, "demodb").option(MAX_SIZE, 40).build());
//	}
//
//}
@Configuration
//@EnableR2dbcRepositories(basePackages = "link.alab.reactiver2dbc")
class DBConfig{
//extends AbstractR2dbcConfiguration{
//	@Bean
////@Profile("test")
//	public ConnectionFactory connectionFactory() {
////		System.out.println(">>>>>>>>>> Using H2 in mem R2DBC connection factory");
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
	
	
	@Configuration
	@RequiredArgsConstructor
	public class PersonController {
		
		private final UserRepository userRepository;	
		 @Bean
		    RouterFunction<ServerResponse> updateEmployeeRoute() {
		      return route(POST("/users"), 
//		        req -> userRepository.findAll()
//		                  .log()
//		                  .then(ok().build()));
		    		  request -> ok().body(userRepository.findAll(), User.class)
					  );
		    }


	}

}
