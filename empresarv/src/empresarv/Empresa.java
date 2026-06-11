/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package empresarv;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author riche
 */
public class Empresa {
    public static void main(String[] args) throws IOException {
        Scanner lea = new Scanner(System.in);
        EmpleadoManager manager = new EmpleadoManager();
        int opcion = 0;

        while(opcion != 7){
            System.out.println("""
                               **************************MENU PRINCIPAL*********************
                               1- Agregar Empleado 
                               2- Listar Empleados no Despedidos
                               3- Agregar ventas a Empleado
                               4- Pagar Empleado
                               5- Ver Reporte Completo de Empleado
                               6- Despedir Empleado
                               7- Salir
                               Escoja una opcion:
                               """);

            String input = lea.nextLine(); 
            try {
                opcion = Integer.parseInt(input);

                switch(opcion){
                    case 1:
                        System.out.print("Nombre del empleado: ");
                        String nombre = lea.nextLine();
                        System.out.print("Salario: ");
                        double salario = 0;
                        try {
                            salario = Double.parseDouble(lea.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Salario invalido. Operacion cancelada.");
                            break;
                        }
                        manager.addEmployee(nombre, salario);
                        System.out.println("Empleado agregado");
                        break;

                    case 2:
                        manager.employeeList();
                        break;

                    case 3:
                        System.out.print("Codigo del empleado: ");
                        int codeVenta = 0;
                        double venta = 0;
                        try {
                            codeVenta = Integer.parseInt(lea.nextLine());
                            System.out.print("Monto de la venta: ");
                            venta = Double.parseDouble(lea.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Codigo o monto invalido. Operacion cancelada.");
                            break;
                        }
                        manager.addSaleToEmployee(codeVenta, venta);
                        break;

                    case 4:
                        System.out.print("Codigo del empleado a pagar: ");
                        int codePago = 0;
                        try {
                            codePago = Integer.parseInt(lea.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Codigo invalido. Operacion cancelada.");
                            break;
                        }
                        manager.payEmployee(codePago);
                        break;

                    case 5:
                        System.out.print("Codigo del empleado para generar reporte: ");
                        int codeReporte = 0;
                        try {
                            codeReporte = Integer.parseInt(lea.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Codigo invalido. Operacion cancelada.");
                            break;
                        }
                        System.out.println("\n------ GENERANDO REPORTE ------");
                        manager.printEmployee(codeReporte);
                        break;

                    case 6:
                        System.out.print("Codigo del empleado a despedir: ");
                        int codeFire = 0;
                        try {
                            codeFire = Integer.parseInt(lea.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Codigo invalido. Operacion cancelada.");
                            break;
                        }
                        manager.fireEmployee(codeFire);
                        break;

                    case 7:
                        System.out.println("Saliendo del sistema...");
                        break;

                    default:
                        System.out.println("Opcion invalida");
                }

            } catch(NumberFormatException e){
                System.out.println("Debe ingresar un numero valido para la opcion.");
                opcion = 0; 
            }
        }

        lea.close();
    }
}