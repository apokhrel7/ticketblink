import { Link } from 'react-router-dom';
import { Calendar, MapPin, Users } from 'lucide-react';

export default function EventCard({ event }) {
  const date = new Date(event.dateTime);
  const formatted = date.toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
  const time = date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });

  return (
    <Link to={`/events/${event.id}`} className="card group hover:border-surface-700 transition-colors duration-300">
      <div className="aspect-[16/9] overflow-hidden bg-surface-800">
        {event.imageUrl ? (
          <img
            src={event.imageUrl}
            alt={event.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-surface-200/20 text-6xl font-bold">
            TB
          </div>
        )}
      </div>

      <div className="p-5 space-y-3">
        <h3 className="text-lg font-semibold text-surface-50 group-hover:text-brand-400 transition-colors line-clamp-1">
          {event.name}
        </h3>

        <div className="space-y-1.5 text-sm text-surface-200/60">
          <div className="flex items-center gap-2">
            <Calendar className="w-3.5 h-3.5 text-brand-500/70" />
            {formatted} · {time}
          </div>
          <div className="flex items-center gap-2">
            <MapPin className="w-3.5 h-3.5 text-brand-500/70" />
            {event.venueName}
          </div>
          <div className="flex items-center gap-2">
            <Users className="w-3.5 h-3.5 text-brand-500/70" />
            {event.availableSeats} / {event.totalSeats} seats left
          </div>
        </div>

        <div className="pt-2 border-t border-surface-800 flex items-center justify-between">
          <span className="text-sm text-surface-200/40">From</span>
          <span className="text-lg font-bold text-brand-400">${event.standardPrice}</span>
        </div>
      </div>
    </Link>
  );
}
