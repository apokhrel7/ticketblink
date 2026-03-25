import { useMemo } from 'react';

const TYPE_COLORS = {
  VIP: { available: 'bg-amber-500/80 hover:bg-amber-400', selected: 'bg-amber-400 ring-2 ring-amber-300', label: 'VIP' },
  PREMIUM: { available: 'bg-brand-500/80 hover:bg-brand-400', selected: 'bg-brand-400 ring-2 ring-brand-300', label: 'Premium' },
  STANDARD: { available: 'bg-sky-500/80 hover:bg-sky-400', selected: 'bg-sky-400 ring-2 ring-sky-300', label: 'Standard' },
};

export default function SeatMap({ seats, selectedIds, onToggle }) {
  const rows = useMemo(() => {
    const map = new Map();
    seats.forEach((seat) => {
      if (!map.has(seat.seatRow)) map.set(seat.seatRow, []);
      map.get(seat.seatRow).push(seat);
    });
    return Array.from(map.entries()).map(([row, seats]) => ({
      row,
      seats: seats.sort((a, b) => a.seatNumber - b.seatNumber),
    }));
  }, [seats]);

  return (
    <div className="space-y-5">
      {/* Screen / Stage indicator */}
      <div className="flex justify-center">
        <div className="w-2/3 h-2 bg-gradient-to-r from-transparent via-brand-500/40 to-transparent rounded-full" />
      </div>
      <p className="text-center text-xs text-surface-200/30 tracking-widest uppercase">Stage</p>

      {/* Seat grid */}
      <div className="space-y-1.5 flex flex-col items-center">
        {rows.map(({ row, seats: rowSeats }) => (
          <div key={row} className="flex items-center gap-1.5">
            <span className="w-6 text-right text-xs text-surface-200/30 font-mono">{row}</span>
            <div className="flex gap-1">
              {rowSeats.map((seat) => {
                const isSelected = selectedIds.has(seat.id);
                const colors = TYPE_COLORS[seat.seatType];
                const base = seat.booked
                  ? 'bg-surface-800 cursor-not-allowed opacity-30'
                  : isSelected
                    ? colors.selected
                    : colors.available;

                return (
                  <button
                    key={seat.id}
                    disabled={seat.booked}
                    onClick={() => onToggle(seat.id)}
                    className={`w-7 h-7 rounded-t-lg text-[10px] font-mono text-white/90 transition-all duration-150 ${base}`}
                    title={`${seat.seatRow}${seat.seatNumber} — ${seat.seatType}${seat.booked ? ' (Booked)' : ''}`}
                  >
                    {seat.seatNumber}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* Legend */}
      <div className="flex justify-center gap-6 pt-3">
        {Object.entries(TYPE_COLORS).map(([type, colors]) => (
          <div key={type} className="flex items-center gap-2">
            <div className={`w-4 h-4 rounded-t-md ${colors.available.split(' ')[0]}`} />
            <span className="text-xs text-surface-200/50">{colors.label}</span>
          </div>
        ))}
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-t-md bg-surface-800 opacity-30" />
          <span className="text-xs text-surface-200/50">Booked</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-t-md bg-white ring-2 ring-white/50" />
          <span className="text-xs text-surface-200/50">Selected</span>
        </div>
      </div>
    </div>
  );
}
