package com.pocketwriter.backend

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import jakarta.annotation.PreDestroy

@SpringBootApplication
class PocketWriterBackendApplication {
	private val logger = LoggerFactory.getLogger(PocketWriterBackendApplication::class.java)
	
	init {
		// Disable DevTools auto restart
		System.setProperty("spring.devtools.restart.enabled", "false")
	}
	
	@Bean
	fun logApplicationStartup(env: Environment, context: ApplicationContext): String {
		// Get the actual server port that was assigned (important when using server.port=0)
		val actualPort = if (context is ServletWebServerApplicationContext) {
			context.webServer.port.toString()
		} else {
			env.getProperty("server.port") ?: "8080"
		}
		
		logger.info("""
			
			----------------------------------------------------------
			Application '${env.getProperty("spring.application.name")}' is running!
			Access URLs:
			- Local:      http://localhost:$actualPort/api/ping
			- Health:     http://localhost:$actualPort/api/health
			- Swagger UI: http://localhost:$actualPort/swagger-ui/index.html
			----------------------------------------------------------
		""".trimIndent())
		return "applicationStartupLogger"
	}
	
	@PreDestroy
	fun onShutdown() {
		logger.info("""
			
			----------------------------------------------------------
			Application is shutting down gracefully...
			----------------------------------------------------------
		""".trimIndent())
	}
}

fun main(args: Array<String>) {
	val logger = LoggerFactory.getLogger(PocketWriterBackendApplication::class.java)
	
	// Disable DevTools restart capabilities to avoid SilentExitException
	System.setProperty("spring.devtools.restart.enabled", "false")
	System.setProperty("spring.devtools.livereload.enabled", "false")
	System.setProperty("spring.devtools.restart.quiet", "true")
	
	try {
		logger.info("Starting Pocket Writer Backend Application")
		val context = runApplication<PocketWriterBackendApplication>(*args) {
			setRegisterShutdownHook(true)
		}
		
		// Register JVM shutdown hook for graceful shutdown
		Runtime.getRuntime().addShutdownHook(Thread {
			logger.info("Received shutdown signal")
			try {
				// Allow Spring to close gracefully
				context.close()
				logger.info("Application has been shut down successfully")
			} catch (e: Exception) {
				logger.error("Error during application shutdown", e)
			}
		})
		
		logger.info("Pocket Writer Backend Application started successfully")
	} catch (e: Exception) {
		// Skip throwing SilentExitException which is expected when using DevTools
		if (e.javaClass.name.contains("SilentExit")) {
			logger.info("DevTools SilentExitException caught - this is expected when using DevTools")
		} else {
			logger.error("Failed to start application: ${e.message}", e)
			throw e
		}
	}
}
