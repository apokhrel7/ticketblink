import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { eventsApi, bookingsApi } from '../api/services';
import { useAuth } from '../context/AuthContext';
import SeatMap from '../components/SeatMap';
import { Calendar, MapPin, Loader2, Check, AlertCircle, X } from 'lucide-react';

export default function EventPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [event, setEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [booking, setBooking] = useState(false);
  const [result, setResult] = useState(null); // { type: 'success'|'error', message }

  useEffect(() => {
    Promise.all([eventsApi.getById(id), eventsApi.getSeats(id)])
      .then(([eventRes, seatsRes]) => {
        setEvent(eventRes.data);
        setSeats(seatsRes.data);
      })
      .catch(() => setResult({ type: 'error', message: 'Failed to load event' }))
      .finally(() => setLoading(false));
  }, [id]);

  const handleToggle = useCallback((seatId) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(seatId)) next.delete(seatId);
      else next.add(seatId);
      return next;
    });
    setResult(null);
  }, []);

  const selectedSeats = seats.filter((s) => selectedIds.has(s.id));

  const totalPrice = selectedSeats.reduce((sum, seat) => {
    if (!event) return sum;
    const price =
      seat.seatType === 'VIP' ? event.vipPrice :
      seat.seatType === 'PREMIUM' ? event.premiumPrice :
      event.standardPrice;
    return sum + parseFloat(price);
  }, 0);

  const handleBook = async () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/events/${id}` } });
      return;
    }

    setBooking(true);
    setResult(null);

    try {
      await bookingsApi.create({ eventId: parseInt(id), seatIds: Array.from(selectedIds) });
      setResult({ type: 'success', message: 'Booking confirmed! Redirecting to your orders...' });
      setTimeout(() => navigate('/orders'), 2000);
    } catch (err) {
      const msg = err.response?.data?.message || 'Booking failed. Please try again.';
      setResult({ type: 'error', message: msg });
      // Refresh seats to show updated availability
      const { data } = await eventsApi.getSeats(id);
      setSeats(data);
      setSelectedIds(new Set());
    } finally {
      setBooking(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-32">
        <Loader2 className="w-8 h-8 text-brand-500 animate-spin" />
      </div>
    );
  }

  if (!event) {
    return <p className="text-center text-red-400 py-32">Event not found</p>;
  }

  const date = new Date(event.dateTime);

  return (
    <div className="max-w-6xl mx-auto px-6 py-10">
      {/* Event header */}
      <div className="grid lg:grid-cols-[1fr_360px] gap-8">
        <div>
          {event.imageUrl && (
            <div className="aspect-[21/9] rounded-xl overflow-hidden mb-6 bg-surface-800">
              <img src={event.imageUrl} alt={event.name} className="w-full h-full object-cover" />
            </div>
          )}

          <h1 className="text-3xl font-bold text-surface-50 mb-3">{event.name}</h1>

          <div className="flex flex-wrap gap-4 text-sm text-surface-200/60 mb-4">
            <div className="flex items-center gap-1.5">
              <Calendar className="w-4 h-4 text-brand-500" />
              {date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
              {' · '}
              {date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
            </div>
            <div className="flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-brand-500" />
              {event.venueName} — {event.venueAddress}
            </div>
          </div>

          {event.description && (
            <p className="text-surface-200/50 leading-relaxed mb-8">{event.description}</p>
          )}

          {/* Price tiers */}
          <div className="flex gap-4 mb-8">
            {[
              { label: 'Standard', price: event.standardPrice, color: 'bg-sky-500/20 text-sky-400 border-sky-500/30' },
              { label: 'Premium', price: event.premiumPrice, color: 'bg-brand-500/20 text-brand-400 border-brand-500/30' },
              { label: 'VIP', price: event.vipPrice, color: 'bg-amber-500/20 text-amber-400 border-amber-500/30' },
            ].map((tier) => (
              <div key={tier.label} className={`px-4 py-2 rounded-lg border text-sm font-medium ${tier.color}`}>
                {tier.label}: ${tier.price}
              </div>
            ))}
          </div>

          {/* Seat map */}
          <div className="card p-6">
            <h2 className="text-lg font-semibold text-surface-50 mb-5">Select Your Seats</h2>
            <SeatMap seats={seats} selectedIds={selectedIds} onToggle={handleToggle} />
          </div>
        </div>

        {/* Booking sidebar */}
        <div className="lg:sticky lg:top-24 lg:self-start">
          <div className="card p-6 space-y-5">
            <h2 className="text-lg font-semibold text-surface-50">Your Selection</h2>

            {selectedSeats.length === 0 ? (
              <p className="text-sm text-surface-200/40">Click on available seats to select them.</p>
            ) : (
              <div className="space-y-2 max-h-60 overflow-y-auto">
                {selectedSeats.map((seat) => {
                  const price =
                    seat.seatType === 'VIP' ? event.vipPrice :
                    seat.seatType === 'PREMIUM' ? event.premiumPrice :
                    event.standardPrice;
                  return (
                    <div key={seat.id} className="flex items-center justify-between text-sm">
                      <div className="flex items-center gap-2">
                        <span className="text-surface-100 font-mono">
                          {seat.seatRow}{seat.seatNumber}
                        </span>
                        <span className="text-surface-200/40 text-xs">{seat.seatType}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-surface-200/60">${price}</span>
                        <button
                          onClick={() => handleToggle(seat.id)}
                          className="text-surface-200/30 hover:text-red-400 transition-colors"
                        >
                          <X className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            <div className="border-t border-surface-800 pt-4 flex items-center justify-between">
              <span className="text-surface-200/60">Total</span>
              <span className="text-2xl font-bold text-brand-400">
                ${totalPrice.toFixed(2)}
              </span>
            </div>

            {/* Status messages */}
            {result && (
              <div className={`flex items-start gap-2 p-3 rounded-lg text-sm ${
                result.type === 'success'
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'bg-red-500/10 text-red-400 border border-red-500/20'
              }`}>
                {result.type === 'success' ? <Check className="w-4 h-4 mt-0.5 shrink-0" /> : <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" />}
                {result.message}
              </div>
            )}

            <button
              onClick={handleBook}
              disabled={selectedSeats.length === 0 || booking}
              className="btn-primary w-full flex items-center justify-center gap-2"
            >
              {booking ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Processing...
                </>
              ) : !isAuthenticated ? (
                'Sign in to Book'
              ) : (
                `Book ${selectedSeats.length} Seat${selectedSeats.length !== 1 ? 's' : ''}`
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
