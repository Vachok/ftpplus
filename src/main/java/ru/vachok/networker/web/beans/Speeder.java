package ru.vachok.networker.web.beans;


/**
 @since 22.08.2018 (9:33) */
public class Speeder {

   private double speed;

   private int road;

   public Speeder(double speed, int road) {
      this.speed = speed;
      this.road = road;
   }

   double getSpeed() {
      return speed;
   }

   void setSpeed(double speed) {
      this.speed = speed;
   }

   int getRoad() {
      return road;
   }

   void setRoad(int road) {
      this.road = road;
   }
}
