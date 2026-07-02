package fr.lebarapp.api.dto;

// Une table du bar renvoyée par l'API (avec le slug encodé dans son QR code).
public record TableResponse(Long id, String label, String qrSlug) {
}
