package com.shopping.admin;

import java.sql.Connection;
import java.util.Scanner;

public interface IAdminAuth {
    void adminLogin(Scanner sc, Connection con);
    void adminSignUp(Scanner sc, Connection con);
    void adminForgotPassword(Scanner sc, Connection con);
    void adminChangePassword(Scanner sc, Connection con);
}