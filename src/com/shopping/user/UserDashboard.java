package com.shopping.user;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import com.shopping.connection.DBConnection;

public class UserDashboard {
    private Scanner sc;

    public UserDashboard(Scanner sc) {
        this.sc = sc;
    }

    public void showDashboard(String email, String username) {
        // Classes initialize
        CartManagemnt cart = new CartManagemnt(); 
        Checkout checkout = new Checkout();
        UserProfile profile = new UserProfile();
        ViewProducts productView = new ViewProducts(); 

        // Connection ni try-with-resources lo initialize chesam
        try (Connection con = DBConnection.getConnection()) {
            while (true) {
                System.out.println("\n***************************************************************************");
                System.out.println("                 WELCOME, " + username.toUpperCase() + " TO ONLINE SHOPPING");
                System.out.println("***************************************************************************");
                System.out.println("1.  View & Edit Profile");
                System.out.println("2.  View Products & Categories");
                System.out.println("3.  Add Product to Cart");
                System.out.println("4.  View Cart ");
                System.out.println("5.  Update Cart");
                System.out.println("6.  Checkout & Payment");
                System.out.println("7.  Verify Delivery (Track Order)");
                System.out.println("8.  Returns / Cancellations / Exchange");
                System.out.println("0.  Logout");
                System.out.println("---------------------------------------------------------------------------");
                System.out.print(" Select Option: ");

                String choice = sc.nextLine().trim();

                if (choice.equals("0")) {
                    System.out.println(" Logged out successfully!");
                    break;
                }

                switch (choice) {
                    case "1":
                        profile.viewAndEditProfile(sc, email);
                        break;

                    case "2":
                        productView.startProductViewing(); 
                        break;

                    case "3":
                        cart.addToCart(sc, email);
                        break;

                    case "4":
                        System.out.println("--- Your Cart Items ---");
                        ViewCart vc = new ViewCart();
                        vc.viewCart(email); 
                        break;

                    case "5":
                        cart.updateCartMenu(sc, email);
                        break;

                    case "6":
                        checkout.processCheckout(sc, email, username);
                        break;

                    case "7":
                        VerfiyDelivery vd = new VerfiyDelivery();
                        vd.verifyUserDelivery(sc, con, username);
                        break;

                    case "8":
                        OrderRequestService.handleOrderRequests(sc, email);
                        break;

                    default:
                        System.out.println(" Invalid Choice! Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(" Database Connection Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" An unexpected error occurred: " + e.getMessage());
        }
    }
}