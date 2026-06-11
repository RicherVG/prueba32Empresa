/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package empresarv;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author riche
 */
public class EmpleadoManager {
    private RandomAccessFile rcods, remps;
    public EmpleadoManager(){
        try{ 
            File fl = new File("company");
            fl.mkdir();
            rcods = new RandomAccessFile("company/codigo.emp", "rw");
            remps = new RandomAccessFile("company/empleados.emp", "rw");
            initCode();     
        }catch(IOException e){    
            System.out.println("Error de inicialización: " + e.getMessage());
        }
    }

    private void initCode() throws IOException{
        if(rcods.length() == 0){ 
            rcods.writeInt(1);
        }
    }
    
    private int getCode() throws IOException{
        rcods.seek(0);
        int code = rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code + 1);
        return code;
    }

    public void addEmployee(String nombre, double salario) throws IOException{
        remps.seek(remps.length()); 
        int code = getCode();
        remps.writeInt(code); 
        remps.writeUTF(nombre);
        remps.writeDouble(salario);
        remps.writeLong(Calendar.getInstance().getTimeInMillis()); 
        remps.writeLong(0);
        createEmployeeFolders(code);
        System.out.println("Empleado agregado con el codigo: " + code);
    }

    private String employeeFolder(int code){
        return "company/empleado" + code;
    }
    
    private RandomAccessFile salesFileFor(int code) throws IOException{
        String dirPadre = employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = dirPadre + "/ventas" + yearActual + ".emp";
        return new RandomAccessFile(path, "rw");
    }

    private void createYearSalesFile(int code) throws IOException{
        RandomAccessFile ryear = salesFileFor(code);
        if(ryear.length() == 0){
            for (int mes = 0; mes < 12; mes++) {
                ryear.writeDouble(0);
                ryear.writeBoolean(false); 
            }
        }
        ryear.close();
    }
    
    private void createEmployeeFolders(int code) throws IOException{
        File dir = new File(employeeFolder(code));
        dir.mkdir();
        createYearSalesFile(code);
    }
    
    public void employeeList() throws IOException{
        remps.seek(0);
        while(remps.getFilePointer() < remps.length()){
            int code = remps.readInt();
            String name = remps.readUTF(); 
            double sal = remps.readDouble();
            Date fecha = new Date(remps.readLong());
            long baja = remps.readLong();
            if(baja == 0){ 
                System.out.println(code + " - " + name + " - $" + sal + " - " + fecha);
            }
        }
    }

    private boolean isEmployeeActive(int code) throws IOException{
        remps.seek(0);
        while(remps.getFilePointer() < remps.length()){
            int codigo = remps.readInt();
            remps.readUTF(); 
            remps.skipBytes(16); 
            long baja = remps.readLong();
            if(codigo == code && baja == 0){
                return true;
            }
        }
        return false;
    }
    
    public void addSaleToEmployee(int code, double ven) throws IOException{
        if(!isEmployeeActive(code)){
            System.out.println("El empleado con el codigo " + code + " no esta activo");
            return;
        }

        RandomAccessFile sales = salesFileFor(code);
        int pos = Calendar.getInstance().get(Calendar.MONTH) * 9;
        sales.seek(pos);
        
        double monto = sales.readDouble();
        sales.seek(pos);
        sales.writeDouble(monto + ven);
        System.out.println("Venta de $" + ven + " agregada. Nuevo acumulado de ventas para el mes: $" + (monto + ven));
        sales.close(); 
    }
    
    public boolean fireEmployee(int code) throws IOException{
        remps.seek(0);
        while(remps.getFilePointer() < remps.length()){
            int codigo = remps.readInt();
            String name = remps.readUTF();
            remps.skipBytes(16); 
            long posBaja = remps.getFilePointer();
            long baja = remps.readLong();
            
            if(codigo == code && baja == 0){
                remps.seek(posBaja);
                remps.writeLong(new Date().getTime());
                System.out.println("Empleado: " + name + " ha sido despedido");
                return true;
            }
        }
        System.out.println("El empleado con el codigo " + code + " no esta activo");
        return false;
    }
    private RandomAccessFile billsFileFor(int code) throws IOException{
        String dir = employeeFolder(code);
        String path = dir + "/recibos.emp";
        return new RandomAccessFile(path, "rw");
    }
    
    public void payEmployee(int code) throws IOException{
        if(!isEmployeeActive(code) || isEmployeePayed(code)){
            System.out.println("No se pudo pagar");
            return;
        }  
        remps.seek(0);
        String name = "";
        double salario = 0;
        while(remps.getFilePointer() < remps.length()){
            int codigo = remps.readInt();
            String nom = remps.readUTF();
            double sal = remps.readDouble();
            remps.skipBytes(16); 
            if(codigo == code){
                name = nom;
                salario = sal;
                break;
            }
        }
        
        Calendar cal = Calendar.getInstance();
        int mes = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        
        RandomAccessFile ventas = salesFileFor(code);
        long pos = mes * 9;
        ventas.seek(pos);
        double totalVentas = ventas.readDouble();
        double sueldo = salario + (totalVentas * 0.10);
        double deduccion = sueldo * 0.035;
        double total = sueldo - deduccion;
        RandomAccessFile bills = billsFileFor(code);
        bills.seek(bills.length());
        bills.writeLong(new Date().getTime());
        bills.writeDouble(sueldo);
        bills.writeDouble(deduccion);
        bills.writeInt(year);
        bills.writeInt(mes + 1);
        ventas.seek(pos + 8);
        ventas.writeBoolean(true);        
        bills.close();
        ventas.close();
        System.out.println("Empleado " + name + " se le pago Lps. " + total);
    }          
    public boolean isEmployeePayed(int code) throws IOException{
        RandomAccessFile sales = salesFileFor(code);
        int mesActual = Calendar.getInstance().get(Calendar.MONTH);
        long pos = mesActual * 9;
        sales.seek(pos);
        sales.skipBytes(8);
        boolean pagado = sales.readBoolean();
        sales.close();
        return pagado;
    }    
    public void printEmployee(int code) throws IOException{
        remps.seek(0);
        String name = "";
        double salario = 0;
        Date fecha = null;
        boolean encontrado = false;
        while(remps.getFilePointer() < remps.length()){
            int codigo = remps.readInt();
            String nom = remps.readUTF();
            double sal = remps.readDouble();
            Date fec = new Date(remps.readLong());
            long baja = remps.readLong();
            if(codigo == code){
                name = nom;
                salario = sal;
                fecha = fec;
                encontrado = true;
                break;
            }
        }

        if(!encontrado){
            System.out.println("Empleado no existe");
            return;
        }
        
        System.out.println("Codigo: " + code);
        System.out.println("Nombre: " + name);
        System.out.println("Salario: " + salario);
        System.out.println("Fecha de contratacion: " + fecha);
        System.out.println("\nVentas del aio actual");
        RandomAccessFile ventas = salesFileFor(code);
        double total = 0;
        for(int mes = 0; mes < 12; mes++){
            ventas.seek(mes * 9);
            double valor = ventas.readDouble();
            System.out.printf("Mes %d : %.2f\n", (mes + 1), valor);
            total += valor;
        }
        ventas.close();
         System.out.printf("\nTotal de ventas del anio: %.2f\n", total);
        RandomAccessFile bills = billsFileFor(code);
        int contador = 0;
        bills.seek(0);
        while(bills.getFilePointer() < bills.length()){
            bills.readLong();   
            bills.readDouble(); 
            bills.readDouble();
            bills.readInt();  
            bills.readInt();   
            contador++;
        }
        bills.close();
        System.out.println("Total de pagos realizados: " + contador);
    }
}

    
