import React, { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate, useLocation } from 'react-router-dom'
import { Hotel, Eye, EyeOff } from 'lucide-react'
import { loginUser, selectIsAuthenticated, selectAuthLoading, selectAuthError, clearError } from '../../store/slices/authSlice'

const schema = yup.object({
  email:    yup.string().email('Invalid email').required('Email is required'),
  password: yup.string().min(6, 'Min 6 characters').required('Password is required'),
})

export default function LoginPage() {
  const dispatch       = useDispatch()
  const navigate       = useNavigate()
  const location       = useLocation()
  const isAuthenticated = useSelector(selectIsAuthenticated)
  const loading        = useSelector(selectAuthLoading)
  const authError      = useSelector(selectAuthError)
  const [showPwd, setShowPwd] = React.useState(false)

  const from = location.state?.from?.pathname ?? '/admin/dashboard'

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: yupResolver(schema),
  })

  useEffect(() => {
    if (isAuthenticated) navigate(from, { replace: true })
    return () => dispatch(clearError())
  }, [isAuthenticated, navigate, from, dispatch])

  const onSubmit = (data) => dispatch(loginUser(data))

  return (
    <div className="min-h-screen bg-gradient-to-br from-hotel-navy to-primary-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white/10 rounded-2xl mb-4">
            <Hotel className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white mb-1">Grand Horizon</h1>
          <p className="text-primary-200 text-sm">Hotel Management System</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-modal p-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">Sign in to your account</h2>

          {authError && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
              {authError}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div>
              <label className="form-label">Email address</label>
              <input {...register('email')} type="email" autoComplete="email"
                className="form-input" placeholder="you@hotel.com" />
              {errors.email && <p className="form-error">{errors.email.message}</p>}
            </div>

            <div>
              <label className="form-label">Password</label>
              <div className="relative">
                <input {...register('password')} type={showPwd ? 'text' : 'password'}
                  autoComplete="current-password" className="form-input pr-10" placeholder="••••••••" />
                <button type="button" onClick={() => setShowPwd(!showPwd)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                  {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <p className="form-error">{errors.password.message}</p>}
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-2.5 mt-2">
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                  Signing in…
                </span>
              ) : 'Sign in'}
            </button>
          </form>

          <p className="mt-4 text-xs text-center text-gray-400">
            Default: admin@grandhorizon.com / Password123!
          </p>
        </div>
      </div>
    </div>
  )
}
