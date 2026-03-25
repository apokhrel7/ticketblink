import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Ticket, LogOut, ShoppingBag, User } from 'lucide-react';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="border-b border-surface-800 bg-surface-950/80 backdrop-blur-md sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2.5 group">
          <Ticket className="w-6 h-6 text-brand-500 group-hover:rotate-12 transition-transform duration-300" />
          <span className="text-xl font-bold tracking-tight">
            Ticket<span className="text-brand-500">Blink</span>
          </span>
        </Link>

        <nav className="flex items-center gap-3">
          {isAuthenticated ? (
            <>
              <div className="hidden sm:flex items-center gap-2 text-sm text-surface-200/60 mr-2">
                <User className="w-3.5 h-3.5" />
                {user.name}
              </div>
              <Link to="/orders" className="btn-outline flex items-center gap-2 text-sm !px-4 !py-2">
                <ShoppingBag className="w-4 h-4" />
                <span className="hidden sm:inline">My Orders</span>
              </Link>
              <button onClick={handleLogout} className="btn-outline flex items-center gap-2 text-sm !px-4 !py-2">
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">Logout</span>
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-outline text-sm !px-4 !py-2">
                Sign In
              </Link>
              <Link to="/register" className="btn-primary text-sm !px-4 !py-2">
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
