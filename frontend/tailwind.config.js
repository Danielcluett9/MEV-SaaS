/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
    "./src/**/*.{vue,js,ts,jsx,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        'mev-blue': '#00D4FF',
        'mev-purple': '#6366F1',
        'mev-green': '#10B981',
        'mev-red': '#EF4444'
      }
    },
  },
  plugins: [],
}
