package me.horlick.db;

public interface Adapter<From, To> {

  To adapt(From from);
}
