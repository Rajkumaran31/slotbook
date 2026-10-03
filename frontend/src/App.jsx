import { useEffect, useState } from "react";
import { api } from "./api.js";

const HOURS = Array.from({ length: 16 }, (_, i) => i + 6); // 6..21
const label = (h) => `${h % 12 || 12}${h < 12 ? "am" : "pm"}`;
const priceFor = (base, h) => (h >= 18 && h <= 20 ? Math.floor(base * 1.4) : h < 8 ? Math.floor(base * 0.8) : base);
const today = () => new Date().toISOString().slice(0, 10);

export default function App() {
  const [name, setName] = useState(localStorage.getItem("name"));
  const [page, setPage] = useState("venues");
  const [toast, setToast] = useState(null);
  const say = (msg, bad) => { setToast({ msg, bad }); setTimeout(() => setToast(null), 3200); };
  const logout = () => { localStorage.clear(); setName(null); setPage("venues"); };

  return (
    <>
      <header className="top">
        <div className="brand" onClick={() => setPage("venues")}>SLOT<span>BOOK</span></div>
        <nav>
          <button className={page === "venues" ? "on" : ""} onClick={() => setPage("venues")}>Pitches</button>
          {name && <button className={page === "mine" ? "on" : ""} onClick={() => setPage("mine")}>My tickets</button>}
          {name ? <button onClick={logout}>Log out ({name})</button> : <button className="cta" onClick={() => setPage("auth")}>Log in</button>}
        </nav>
      </header>
      <main>
        {page === "auth" && <Auth say={say} done={(n) => { setName(n); setPage("venues"); }} />}
        {page === "venues" && <Venues loggedIn={!!name} say={say} goLogin={() => setPage("auth")} goTickets={() => setPage("mine")} />}
        {page === "mine" && <Tickets say={say} />}
      </main>
      {toast && <div className={"toast " + (toast.bad ? "bad" : "")}>{toast.msg}</div>}
    </>
  );
}

function Auth({ done, say }) {
  const [signup, setSignup] = useState(false);
  const [f, setF] = useState({ name: "", email: "", password: "" });
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  const submit = async () => {
    try {
      const r = await api(signup ? "/auth/register" : "/auth/login", { method: "POST", body: f });
      localStorage.setItem("token", r.token); localStorage.setItem("name", r.name);
      say("Welcome, " + r.name); done(r.name);
    } catch (e) { say(e.message, true); }
  };
  return (
    <section className="auth">
      <h1>{signup ? "Join the squad" : "Welcome back"}</h1>
      <p className="muted">One account to book every pitch, court and table in town.</p>
      {signup && <input placeholder="Your name" value={f.name} onChange={set("name")} />}
      <input placeholder="Email" type="email" value={f.email} onChange={set("email")} />
      <input placeholder="Password (6+ characters)" type="password" value={f.password} onChange={set("password")} onKeyDown={(e) => e.key === "Enter" && submit()} />
      <button className="cta wide" onClick={submit}>{signup ? "Create account" : "Log in"}</button>
      <p className="muted link" onClick={() => setSignup(!signup)}>{signup ? "Already have an account? Log in" : "New here? Create an account"}</p>
    </section>
  );
}

function Venues({ loggedIn, say, goLogin, goTickets }) {
  const [venues, setVenues] = useState([]);
  const [sport, setSport] = useState("All");
  const [open, setOpen] = useState(null);
  useEffect(() => { api("/venues").then(setVenues).catch(() => say("Backend is not running on port 8080", true)); }, []);
  const sports = ["All", ...new Set(venues.map((v) => v.sport))];
  return (
    <>
      <section className="hero">
        <h1>Your game.<br />Your hour.<br />Locked in.</h1>
        <p>Pick a pitch, tap a slot, get a ticket. Peak hours glow, taken slots go dark, double bookings are impossible.</p>
      </section>
      <div className="chips">{sports.map((s) => <button key={s} className={s === sport ? "on" : ""} onClick={() => setSport(s)}>{s}</button>)}</div>
      <div className="grid">
        {venues.filter((v) => sport === "All" || v.sport === sport).map((v) => (
          <article className="venue" key={v.id} onClick={() => (loggedIn ? setOpen(v) : (say("Log in to book a slot", true), goLogin()))}>
            <div className="emoji">{v.emoji}</div>
            <h3>{v.name}</h3>
            <p className="muted">{v.about}</p>
            <div className="row"><span className="tag">{v.sport}</span><b>₹{v.basePrice}<small>/hr</small></b></div>
          </article>
        ))}
      </div>
      {open && <Picker venue={open} close={() => setOpen(null)} say={say} goTickets={goTickets} />}
    </>
  );
}

function Picker({ venue, close, say, goTickets }) {
  const [date, setDate] = useState(today());
  const [taken, setTaken] = useState([]);
  const [hour, setHour] = useState(null);
  const [ticket, setTicket] = useState(null);
  const load = () => api(`/venues/${venue.id}/slots?date=${date}`).then(setTaken);
  useEffect(() => { setHour(null); load(); }, [date]);
  const nowH = new Date().getHours();
  const disabled = (h) => taken.includes(h) || (date === today() && h <= nowH);
  const book = async () => {
    try { setTicket(await api("/bookings", { method: "POST", body: { venueId: venue.id, date, hour } })); }
    catch (e) { say(e.message, true); load(); setHour(null); }
  };
  return (
    <div className="overlay" onClick={close}>
      <div className="sheet" onClick={(e) => e.stopPropagation()}>
        {ticket ? (
          <div className="ticket">
            <div className="stub"><span>{ticket.emoji}</span><h2>{ticket.venueName}</h2></div>
            <div className="cut" />
            <div className="info">
              <p className="muted">{ticket.bookingDate} at {label(ticket.slotHour)} to {label(ticket.slotHour + 1)}</p>
              <div className="code">{ticket.code}</div>
              <p className="muted">Show this code at the venue. Paid: ₹{ticket.price}</p>
              <button className="cta wide" onClick={() => { close(); goTickets(); }}>See all my tickets</button>
            </div>
          </div>
        ) : (
          <>
            <div className="row"><h2>{venue.emoji} {venue.name}</h2><button className="x" onClick={close}>✕</button></div>
            <input type="date" min={today()} value={date} onChange={(e) => setDate(e.target.value)} />
            <div className="board">
              {HOURS.map((h) => (
                <button key={h} disabled={disabled(h)} className={`slot ${h >= 18 && h <= 20 ? "peak" : ""} ${hour === h ? "pick" : ""}`} onClick={() => setHour(h)}>
                  <b>{label(h)}</b><small>₹{priceFor(venue.basePrice, h)}</small>
                </button>
              ))}
            </div>
            <p className="muted legend"><i className="peak" /> peak hours cost 40% more &nbsp; <i className="off" /> taken or past</p>
            <button className="cta wide" disabled={hour === null} onClick={book}>{hour === null ? "Pick a slot" : `Book ${label(hour)} for ₹${priceFor(venue.basePrice, hour)}`}</button>
          </>
        )}
      </div>
    </div>
  );
}

function Tickets({ say }) {
  const [list, setList] = useState(null);
  const load = () => api("/bookings/my").then(setList).catch((e) => say(e.message, true));
  useEffect(() => { load(); }, []);
  const cancel = async (id) => { try { await api("/bookings/" + id, { method: "DELETE" }); say("Booking cancelled"); load(); } catch (e) { say(e.message, true); } };
  if (!list) return <p className="muted pad">Loading your tickets…</p>;
  if (!list.length) return <p className="empty">No tickets yet. Head to Pitches and grab your first slot.</p>;
  return (
    <section>
      <h1 className="h1">My tickets</h1>
      <div className="grid">
        {list.map((b) => (
          <article className="venue mine" key={b.id}>
            <div className="emoji">{b.emoji}</div>
            <h3>{b.venueName}</h3>
            <p className="muted">{b.bookingDate} · {label(b.slotHour)} to {label(b.slotHour + 1)}</p>
            <div className="row"><span className="code sm">{b.code}</span><b>₹{b.price}</b></div>
            <button className="ghost" onClick={() => cancel(b.id)}>Cancel booking</button>
          </article>
        ))}
      </div>
    </section>
  );
}
