package org.jfree;

public class Car {
// Company Names,Cars Names,Engines,CC/Battery Capacity,HorsePower,Total Speed,
// Performance(0 - 100 )KM/H,Cars Prices,Fuel Types,Seats,Torque
private String company;
private String model;
private String engine;
private String cc;
private int horsePower;
private int maxSpeed;
private int performance;
private int price;
private String fuelType;
private int seats;
private String torque;

public Car(String company, String model, String engine, String cc, int horsePower, int maxSpeed,
           int performance, int price, String fuelType, int seats, String torque) {
    this.company = company;
    this.model = model;
    this.engine = engine;
    this.cc = cc;
    this.horsePower = horsePower;
    this.maxSpeed = maxSpeed;
    this.performance = performance;
    this.price = price;
    this.fuelType = fuelType;
    this.seats = seats;
    this.torque = torque;
    }

public String toString() {
    
    return String.format("%s %s | Engine: %s | CC: %s | Horse Power: %d | Max Speed: %d | Performance: %d | Price: %d | Fuel: %s | Seats: %d | Torque: %s",
        this.company, this.model, this.engine, this.cc, this.horsePower, this.maxSpeed,
        this.performance, this.price, this.fuelType, this.seats, this.torque);
}
}
