import type { Config } from 'tailwindcss'
import animate from 'tailwindcss-animate'

export default {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Playfair Display"', 'Georgia', 'serif'],
        body: ['"Inter"', 'system-ui', 'sans-serif'],
      },
      colors: {
        gold: '#D4A647',
        // Background scale — white/light
        void:    '#FFFFFF',
        deep:    '#EAEAED',
        surface: '#FFFFFF',
        elevated:'#F3F4F6',
        hover:   '#F0F0F0',
        // Borders
        border: {
          DEFAULT: '#E5E7EB',
          red:    '#FECACA',
          strong: '#D1D5DB',
        },
        // Primary accent — red
        red: {
          DEFAULT: '#DC2626',
          light:   '#EF4444',
          dim:     '#B91C1C',
          muted:   '#991B1B',
          pale:    '#FEF2F2',
          glow:    '#FEE2E2',
        },
        // Text scale
        ink: {
          DEFAULT: '#111827',
          dim:     '#6B7280',
          muted:   '#9CA3AF',
        },
      },
      backgroundImage: {
        'lounge-bg': 'radial-gradient(ellipse 80% 40% at 50% 0%, #FEE2E210 0%, transparent 60%)',
        'red-shimmer': 'linear-gradient(135deg, #DC262600 0%, #DC262615 50%, #DC262600 100%)',
      },
      boxShadow: {
        'card':    '0 1px 3px #0000000D, 0 1px 2px #0000000A',
        'card-lg': '0 4px 16px #0000001A, 0 1px 4px #00000010',
        'red':     '0 0 0 3px #DC262620',
        'red-sm':  '0 2px 8px #DC262625',
      },
      borderRadius: {
        DEFAULT: '6px',
        sm:  '4px',
        lg:  '10px',
        xl:  '14px',
        '2xl': '18px',
      },
      animation: {
        'fade-in':  'fadeIn 0.3s ease forwards',
        'slide-up': 'slideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards',
        'shimmer':  'shimmer 1.6s ease-in-out infinite',
      },
      keyframes: {
        fadeIn:   { from: { opacity: '0' }, to: { opacity: '1' } },
        slideUp:  { from: { opacity: '0', transform: 'translateY(12px)' }, to: { opacity: '1', transform: 'translateY(0)' } },
        shimmer:  { '0%': { backgroundPosition: '-200% center' }, '100%': { backgroundPosition: '200% center' } },
      },
      screens: { xs: '375px' },
    },
  },
  plugins: [animate],
} satisfies Config
