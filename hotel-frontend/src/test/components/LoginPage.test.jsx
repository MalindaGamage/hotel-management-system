import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import { configureStore } from '@reduxjs/toolkit'
import { describe, it, expect, vi } from 'vitest'
import LoginPage from '../../pages/auth/LoginPage'
import authReducer from '../../store/slices/authSlice'

const makeStore = (preloadedState = {}) =>
  configureStore({
    reducer: { auth: authReducer },
    preloadedState,
  })

const renderWithProviders = (ui, { store = makeStore() } = {}) =>
  render(
    <Provider store={store}>
      <MemoryRouter>{ui}</MemoryRouter>
    </Provider>
  )

describe('LoginPage', () => {
  it('renders email and password fields', () => {
    renderWithProviders(<LoginPage />)
    expect(screen.getByPlaceholderText(/you@hotel.com/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/••••••••/i)).toBeInTheDocument()
  })

  it('shows validation errors when submitted empty', async () => {
    renderWithProviders(<LoginPage />)
    const button = screen.getByRole('button', { name: /sign in/i })
    await userEvent.click(button)
    await waitFor(() => {
      expect(screen.getByText(/email is required/i)).toBeInTheDocument()
    })
  })

  it('shows error for invalid email format', async () => {
    renderWithProviders(<LoginPage />)
    await userEvent.type(screen.getByPlaceholderText(/you@hotel.com/i), 'notanemail')
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }))
    await waitFor(() => {
      expect(screen.getByText(/invalid email/i)).toBeInTheDocument()
    })
  })
})
