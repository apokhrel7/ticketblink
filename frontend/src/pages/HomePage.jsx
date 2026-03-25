import { useState, useEffect } from 'react';
import { eventsApi } from '../api/services';
import EventCard from '../components/EventCard';
import { Ticket, Loader2 } from 'lucide-react';

export default function HomePage() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    eventsApi.getUpcoming()
      .then(({ data }) => setEvents(data))
      .catch(() => setError('Failed to load events'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      {/* Hero */}
      <div className="text-center mb-14">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-brand-500/10 border border-brand-500/20 text-brand-400 text-sm font-medium mb-6">
          <Ticket className="w-4 h-4" />
          Live Events
        </div>
        <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-surface-50 mb-4">
          Grab Your Seat <br className="hidden sm:block" />
          <span className="text-brand-500">Before It Blinks</span>
        </h1>
        <p className="text-surface-200/50 max-w-lg mx-auto">
          Pick your exact seat, book in seconds, and never miss the events that matter to you.
        </p>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 text-brand-500 animate-spin" />
        </div>
      ) : error ? (
        <p className="text-center text-red-400 py-20">{error}</p>
      ) : events.length === 0 ? (
        <p className="text-center text-surface-200/40 py-20">No upcoming events right now. Check back soon!</p>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  );
}
