package com.mcmm.service;

import com.mcmm.model.dto.NotificacionBadgeDto;

public interface INotificacion {
    NotificacionBadgeDto getBadge(Long iglesiaId);
}
