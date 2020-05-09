package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    validarMontoPositivo(cuanto);

    if (this.depositosRealizados() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    new Movimiento(LocalDate.now(), cuanto, true).agregateA(this);
  }

  public void sacar(double monto) {
    validarMontoPositivo(monto);
    validarNoExtraerMasDelSaldoQueHay(monto);
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    validarNoExtraeMasDelLimite(monto, limite);
    new Movimiento(LocalDate.now(), monto, false).agregateA(this);
  }

  private void validarNoExtraeMasDelLimite(double cuanto, double limite) {
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }

  private void validarNoExtraerMasDelSaldoQueHay(double cuanto) {
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }


  public double getMontoExtraidoA(LocalDate fecha) {
    return this.movimientosDeFecha(fecha).stream().mapToDouble(Movimiento::getMonto).sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  private List<Movimiento> movimientosDeFecha(LocalDate fecha){
    return (List<Movimiento>) this.obtenerExtracciones().stream().filter(movimiento -> movimiento.getFecha().equals(fecha));
  }
  private List<Movimiento> obtenerDepositos(){
    return (List<Movimiento>) this.getMovimientos().stream().filter(movimiento -> movimiento.isDeposito());
  }
  private List<Movimiento> obtenerExtracciones(){
    return (List<Movimiento>) this.getMovimientos().stream().filter(movimiento -> !movimiento.isDeposito());
  }
  private long depositosRealizados(){
    return this.obtenerDepositos().size();
  }

  private void validarMontoPositivo(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
  }
}
