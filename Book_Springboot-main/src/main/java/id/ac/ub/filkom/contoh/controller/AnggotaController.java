package id.ac.ub.filkom.contoh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.ub.filkom.contoh.service.AnggotaService;

/** Controller: halaman lihat anggota. */
@Controller
public class AnggotaController {

    private final AnggotaService anggotaService;

    public AnggotaController(AnggotaService anggotaService) {
        this.anggotaService = anggotaService;
    }

    // GET http://localhost:8080/anggota
    @GetMapping("/anggota")
    public String lihatAnggota(Model model) {
        model.addAttribute("daftarAnggota", anggotaService.lihatSemuaAnggota());
        return "anggota";            // -> templates/anggota.html
    }

    @PostMapping("/anggota")
    public String tambahAnggota(@RequestParam String idAnggota,
                                @RequestParam String nama,
                                @RequestParam int batasPinjam,
                                RedirectAttributes redirectAttributes) {
        try {
            anggotaService.tambahAnggota(idAnggota, nama, batasPinjam);
            redirectAttributes.addFlashAttribute("pesanSukses",
                    "Anggota berhasil ditambahkan.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("pesanError", exception.getMessage());
        }
        return "redirect:/anggota";
    }
}