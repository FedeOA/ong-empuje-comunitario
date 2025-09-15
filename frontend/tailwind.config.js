/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        "empuje-green": "#16a34a",
        "empuje-blue": "#1e40af",
        "empuje-orange": "#f97316",
        "empuje-bg": "#f3f4f6",       // fondo principal
        "empuje-navbar": "#fff7ed",   // fondo crema para Navbar
        "empuje-dark": "#1f2937"      // texto oscuro
      }
    }
  },
  plugins: [],
}
