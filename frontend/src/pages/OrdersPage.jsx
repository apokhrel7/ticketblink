import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { bookingsApi } from '../api/services';
import { Loader2, ShoppingBag, XCircle, CheckCircle2, Ticket } from 'lucide-react';

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(null);

  useEffect(() => {
    bookingsApi.getOrders()
      .then(({ data }) => setOrders(data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleCancel = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order? This cannot be undone.')) return;
    setCancelling(orderId);
    try {
      const { data } = await bookingsApi.cancel(orderId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? data : o)));
    } catch {
      alert('Failed to cancel order');
    } finally {
      setCancelling(null);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-32">
        <Loader2 className="w-8 h-8 text-brand-500 animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold text-surface-50 mb-8 flex items-center gap-3">
        <ShoppingBag className="w-7 h-7 text-brand-500" />
        My Orders
      </h1>

      {orders.length === 0 ? (
        <div className="text-center py-20">
          <Ticket className="w-12 h-12 text-surface-200/20 mx-auto mb-4" />
          <p className="text-surface-200/40 mb-4">You haven't booked any events yet.</p>
          <Link to="/" className="btn-primary inline-block">Browse Events</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => {
            const isCancelled = order.status === 'CANCELLED';
            const date = new Date(order.eventDateTime);

            return (
              <div key={order.id} className={`card p-5 ${isCancelled ? 'opacity-60' : ''}`}>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-surface-50">{order.eventName}</h3>
                      <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium ${
                        isCancelled
                          ? 'bg-red-500/10 text-red-400 border border-red-500/20'
                          : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      }`}>
                        {isCancelled ? <XCircle className="w-3 h-3" /> : <CheckCircle2 className="w-3 h-3" />}
                        {order.status}
                      </span>
                    </div>

                    <p className="text-sm text-surface-200/50">
                      {date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
                      {' · '}
                      {date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                    </p>

                    <div className="flex flex-wrap gap-1.5 pt-1">
                      {order.tickets.map((t) => (
                        <span
                          key={t.id}
                          className="px-2 py-0.5 rounded text-xs font-mono bg-surface-800 text-surface-200/60"
                        >
                          {t.seatRow}{t.seatNumber} <span className="text-surface-200/30">({t.seatType})</span>
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-center gap-4 sm:flex-col sm:items-end">
                    <span className="text-xl font-bold text-brand-400">
                      ${order.totalAmount}
                    </span>
                    {!isCancelled && (
                      <button
                        onClick={() => handleCancel(order.id)}
                        disabled={cancelling === order.id}
                        className="btn-outline !px-3 !py-1.5 text-xs text-red-400 border-red-500/30 hover:border-red-500 hover:text-red-300"
                      >
                        {cancelling === order.id ? (
                          <Loader2 className="w-3 h-3 animate-spin" />
                        ) : (
                          'Cancel'
                        )}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
