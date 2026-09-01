package com.railways.blockplanning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI-Powered Automatic Block Planning System
 * SIH Problem Statement 26027 — Ministry of Railways
 *
 * NOTE: All source system data (TMS, SMMS, TDMS, BDMS, COA) is synthetic.
 * The adapter layer is designed to be swapped for real API clients when access is granted.
 */
@SpringBootApplication
@EnableScheduling
public class BlockPlanningApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockPlanningApplication.class, args);
    }
}
