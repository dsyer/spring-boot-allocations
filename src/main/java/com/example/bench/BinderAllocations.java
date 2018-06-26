package com.example.bench;

import java.io.IOException;

public class BinderAllocations {

	public static void main(String[] args) throws IOException {
		while (true) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < 1000; i++) {
				new BinderBenchmark.WrappedState().run();
			}
			long end = System.currentTimeMillis();
			System.out.println("Duration: " + (end - start));
		}
	}

}