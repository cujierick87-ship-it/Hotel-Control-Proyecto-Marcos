package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RecepcionController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private HabitacionService habitacionService;

    // 1. Cargar el panel principal con TODO (CU-04 y CU-05)
    @GetMapping("/recepcion/panel")
    public String panelRecepcion(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        return "panel_recepcion";
    }

    // 2. Buscar reserva por cédula (CU-05 Extend)
    @PostMapping("/recepcion/reservas/buscar")
    public String buscarReserva(String cedula, Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("reservas", reservaService.buscarPorCedula(cedula));
        return "panel_recepcion";
    }

    // 3. Botones de acción: Confirmar, Check-in, Check-out (CU-06)
    @GetMapping("/recepcion/reservas/accion/{accion}/{id}")
    public String accionReserva(@PathVariable String accion, @PathVariable Long id) {
        reservaService.procesarAccionRecepcion(id, accion);
        return "redirect:/recepcion/panel";
    }

    // 4. Botón para terminar limpieza (CU-04 Extend)
    @GetMapping("/recepcion/habitaciones/limpiar/{id}")
    public String limpiarHabitacion(@PathVariable Long id) {
        habitacionService.marcarComoLimpia(id);
        return "redirect:/recepcion/panel";
    }
}