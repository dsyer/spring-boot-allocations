package com.example.bench;

import java.io.IOException;

public class BeanCreationAllocations {

	public static void main(String[] args) throws IOException {
		while (true) {
			long start = System.currentTimeMillis();
			BeanCreationBenchmark.ProcessorState state = new BeanCreationBenchmark.ProcessorState();
			for (int i = 0; i < 1000; i++) {
				state.start();
				state.run();
				state.clear();
			}
			long end = System.currentTimeMillis();
			System.out.println("Duration: " + (end - start));
		}
	}

}