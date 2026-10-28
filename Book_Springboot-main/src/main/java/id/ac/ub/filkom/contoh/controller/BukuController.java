package id.ac.ub.filkom.contoh.controller;

import id.ac.ub.filkom.contoh.service.BukuService;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller: terima request, panggil service, pilih template. */
@Controller
public class BukuController {

    private final BukuService bukuService;

    public BukuController(BukuService bukuService) {
        this.bukuService = bukuService;
    }

    // GET http://localhost:8080/buku?kata=java
    @GetMapping("/buku")
    public String daftarBuku(@RequestParam(required = false) String kata,
                             @RequestParam(required = false, defaultValue = "semua") String status,
                             @RequestParam(required = false, defaultValue = "judul") String urutan,
                             Model model) {
        if (!status.equals("semua") && !status.equals("tersedia") && !status.equals("habis")) {
            status = "semua";
        }
        if (!urutan.equals("judul") && !urutan.equals("stok")) {
            urutan = "judul";
        }
        model.addAttribute("daftarBuku", bukuService.urutkanBuku(bukuService.cariBuku(kata, status), urutan));
        model.addAttribute("kata", kata);
        model.addAttribute("status", status);
        model.addAttribute("urutan", urutan);
        model.addAttribute("jumlahJudul", bukuService.jumlahJudul());
        model.addAttribute("jumlahBukuTersedia", bukuService.jumlahBukuTersedia());
        model.addAttribute("jumlahBukuHabis", bukuService.jumlahBukuHabis());
        model.addAttribute("totalStok", bukuService.totalStok());
        return "buku";               // -> templates/buku.html
    }

    @GetMapping("/buku/export")
    public ResponseEntity<byte[]> eksporBuku(@RequestParam(required = false) String kata,
                                             @RequestParam(required = false, defaultValue = "semua") String status,
                                             @RequestParam(required = false, defaultValue = "judul") String urutan) {
        if (!status.equals("semua") && !status.equals("tersedia") && !status.equals("habis")) {
            status = "semua";
        }
        if (!urutan.equals("judul") && !urutan.equals("stok")) {
            urutan = "judul";
        }

        StringBuilder csv = new StringBuilder("\uFEFFID,Judul,Stok,Status\n");
        for (var buku : bukuService.urutkanBuku(bukuService.cariBuku(kata, status), urutan)) {
            csv.append(nilaiCsv(buku.getIdBuku())).append(',')
                    .append(nilaiCsv(buku.getJudul())).append(',')
                    .append(buku.getStok()).append(',')
                    .append(nilaiCsv(buku.getStok() == 0 ? "Habis"
                            : (buku.getStok() == 1 ? "Stok menipis" : "Tersedia")))
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"daftar-buku.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String nilaiCsv(String nilai) {
        return "\"" + nilai.replace("\"", "\"\"") + "\"";
    }

    // POST dari form tambah buku
    @PostMapping("/buku")
    public String tambahBuku(@RequestParam String idBuku,
                             @RequestParam String judul,
                             @RequestParam int stok) {
        bukuService.tambahBuku(idBuku, judul, stok);
        return "redirect:/buku";     // pola Post-Redirect-Get
    }

    @PostMapping("/buku/{idBuku}/pinjam")
    public String pinjamBuku(@PathVariable String idBuku,
                             RedirectAttributes redirectAttributes) {
        try {
            bukuService.pinjamBuku(idBuku);
            redirectAttributes.addFlashAttribute("pesanSukses", "Buku berhasil dipinjam.");
        } catch (IllegalStateException | java.util.NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("pesanError", exception.getMessage());
        }
        return "redirect:/buku";
    }

    @PostMapping("/buku/{idBuku}/kembalikan")
    public String kembalikanBuku(@PathVariable String idBuku,
                                 RedirectAttributes redirectAttributes) {
        try {
            bukuService.kembalikanBuku(idBuku);
            redirectAttributes.addFlashAttribute("pesanSukses", "Buku berhasil dikembalikan.");
        } catch (java.util.NoSuchElementException exception) {
            redirectAttributes.addFlashAttribute("pesanError", exception.getMessage());
        }
        return "redirect:/buku";
    }
}
