import api from './client';

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

export const eventsApi = {
  getUpcoming: () => api.get('/events'),
  getAll: () => api.get('/events/all'),
  getById: (id) => api.get(`/events/${id}`),
  getSeats: (id) => api.get(`/events/${id}/seats`),
};

export const bookingsApi = {
  create: (data) => api.post('/bookings', data),
  getOrders: () => api.get('/bookings/orders'),
  cancel: (orderId) => api.post(`/bookings/orders/${orderId}/cancel`),
};
