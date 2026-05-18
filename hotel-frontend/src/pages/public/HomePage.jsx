import React from 'react'
import { Link } from 'react-router-dom'
import { Star, Wifi, Coffee, Car, Dumbbell, Waves, ArrowRight } from 'lucide-react'

const FEATURES = [
  { icon: Wifi,     label: 'Free WiFi',       desc: 'High-speed internet throughout the property' },
  { icon: Coffee,   label: 'Fine Dining',      desc: 'Award-winning restaurant and 24/7 room service' },
  { icon: Car,      label: 'Valet Parking',    desc: 'Complimentary for all guests' },
  { icon: Dumbbell, label: 'Fitness Center',   desc: 'State-of-the-art gym, open 24 hours' },
  { icon: Waves,    label: 'Rooftop Pool',     desc: 'Heated infinity pool with ocean views' },
]

const ROOMS = [
  { name: 'Standard Single', price: 89,  img: 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600', desc: 'Comfortable city-view room' },
  { name: 'Standard Double', price: 129, img: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600', desc: 'Spacious queen bed with city views' },
  { name: 'Deluxe Suite',    price: 299, img: 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=600', desc: 'Elegant suite with ocean-view balcony' },
]

export default function HomePage() {
  return (
    <div className="min-h-screen bg-white font-sans">
      {/* Nav */}
      <nav className="fixed top-0 inset-x-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <span className="font-display text-xl font-bold text-hotel-navy">Grand Horizon</span>
          <div className="hidden md:flex items-center gap-6 text-sm text-gray-600">
            <a href="#rooms" className="hover:text-gray-900">Rooms</a>
            <a href="#amenities" className="hover:text-gray-900">Amenities</a>
            <Link to="/login" className="text-gray-500 hover:text-gray-900">Staff Login</Link>
            <Link to="/booking" className="btn-primary">Book Now</Link>
          </div>
          <Link to="/booking" className="md:hidden btn-primary text-sm">Book</Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="relative min-h-screen flex items-center justify-center bg-gradient-to-br from-hotel-navy via-primary-900 to-primary-700 overflow-hidden pt-16">
        <div className="absolute inset-0 opacity-20" style={{ backgroundImage: 'url(https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1600)', backgroundSize: 'cover', backgroundPosition: 'center' }} />
        <div className="relative z-10 text-center px-4 max-w-4xl mx-auto">
          <div className="flex justify-center mb-4">
            {[...Array(5)].map((_, i) => <Star key={i} size={20} className="text-accent-400 fill-accent-400" />)}
          </div>
          <h1 className="font-display text-5xl sm:text-7xl font-bold text-white mb-6 leading-tight">
            Where Luxury<br />Meets the Sea
          </h1>
          <p className="text-xl text-primary-200 mb-10 max-w-xl mx-auto">
            Experience unparalleled comfort and breathtaking ocean views at Miami's finest beachfront hotel.
          </p>
          <Link to="/booking" className="inline-flex items-center gap-2 bg-accent-500 hover:bg-accent-600 text-white px-8 py-4 rounded-xl text-lg font-semibold transition-colors">
            Reserve Your Stay <ArrowRight size={20} />
          </Link>
        </div>
      </section>

      {/* Rooms */}
      <section id="rooms" className="py-20 bg-hotel-cream">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="font-display text-4xl font-bold text-hotel-navy mb-3">Our Accommodations</h2>
            <p className="text-gray-500 text-lg">Choose from our expertly designed rooms and suites</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {ROOMS.map(room => (
              <div key={room.name} className="bg-white rounded-2xl overflow-hidden shadow-card hover:shadow-card-hover transition-shadow">
                <div className="relative h-52 overflow-hidden">
                  <img src={room.img} alt={room.name} className="w-full h-full object-cover" loading="lazy" />
                </div>
                <div className="p-6">
                  <h3 className="font-semibold text-lg text-gray-900 mb-1">{room.name}</h3>
                  <p className="text-sm text-gray-500 mb-4">{room.desc}</p>
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-2xl font-bold text-hotel-navy">${room.price}</span>
                      <span className="text-sm text-gray-400">/night</span>
                    </div>
                    <Link to="/booking" className="btn-primary text-sm">Book Now</Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Amenities */}
      <section id="amenities" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="font-display text-4xl font-bold text-hotel-navy mb-3">World-Class Amenities</h2>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
            {FEATURES.map(({ icon: Icon, label, desc }) => (
              <div key={label} className="text-center p-4">
                <div className="inline-flex items-center justify-center w-14 h-14 bg-primary-50 rounded-2xl mb-3">
                  <Icon className="text-primary-600" size={24} />
                </div>
                <h4 className="font-semibold text-sm text-gray-900 mb-1">{label}</h4>
                <p className="text-xs text-gray-400 hidden md:block">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-hotel-navy text-white py-12">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <p className="font-display text-2xl font-bold mb-2">Grand Horizon Hotel</p>
          <p className="text-primary-300 text-sm">123 Ocean Boulevard, Miami, FL 33101</p>
          <p className="text-primary-400 text-xs mt-4">© {new Date().getFullYear()} Grand Horizon Hotel. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}
