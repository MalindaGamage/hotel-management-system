import React, { useEffect, useState, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Plus, Star } from 'lucide-react'
import { fetchGuests, createGuest, selectGuests, selectGuestsLoading } from '../../store/slices/guestSlice'
import DataTable from '../../components/common/DataTable'
import Badge from '../../components/common/Badge'
import Modal from '../../components/common/Modal'
import SearchFilter from '../../components/common/SearchFilter'
import { useForm } from 'react-hook-form'

export default function GuestsPage() {
  const dispatch = useDispatch()
  const guests   = useSelector(selectGuests)
  const loading  = useSelector(selectGuestsLoading)
  const [showCreate, setShowCreate] = useState(false)
  const [search, setSearch] = useState('')
  const [page, setPage]     = useState(0)
  const [detail, setDetail] = useState(null)

  const load = useCallback(() => {
    dispatch(fetchGuests({ search, page, size: 20 }))
  }, [dispatch, search, page])

  useEffect(() => { load() }, [load])

  const { register, handleSubmit, reset, formState: { errors } } = useForm()

  const onCreateGuest = (data) => {
    dispatch(createGuest(data))
    setShowCreate(false)
    reset()
  }

  const columns = [
    { key: 'firstName', header: 'Name', render: (_, r) => (
      <div>
        <p className="font-medium">{r.firstName} {r.lastName}</p>
        <p className="text-xs text-gray-400">{r.email}</p>
      </div>
    )},
    { key: 'phone',        header: 'Phone' },
    { key: 'nationality',  header: 'Nationality' },
    { key: 'loyaltyTier',  header: 'Tier',    render: (v) => <Badge status={v} /> },
    { key: 'loyaltyPoints',header: 'Points',  render: (v) => (
      <span className="flex items-center gap-1 text-yellow-600"><Star size={12} />{v?.toLocaleString()}</span>
    )},
    { key: 'isVerified',   header: 'Verified', render: (v) => (
      <span className={`badge ${v ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
        {v ? 'Verified' : 'Unverified'}
      </span>
    )},
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{guests?.totalElements ?? 0} guests</p>
        <button onClick={() => setShowCreate(true)} className="btn-primary"><Plus size={15} /> Add Guest</button>
      </div>

      <SearchFilter placeholder="Search by name, email, or phone..." onSearch={setSearch} />

      <DataTable
        columns={columns}
        data={guests?.content ?? []}
        loading={loading}
        pagination={{ page, size: 20, totalElements: guests?.totalElements ?? 0, totalPages: guests?.totalPages ?? 0 }}
        onPageChange={setPage}
        onRowClick={setDetail}
        emptyMessage="No guests found"
      />

      {/* Create Modal */}
      <Modal isOpen={showCreate} onClose={() => { setShowCreate(false); reset() }} title="New Guest Profile" size="lg"
        footer={<>
          <button className="btn-secondary" onClick={() => { setShowCreate(false); reset() }}>Cancel</button>
          <button className="btn-primary" form="create-guest-form" type="submit">Save Guest</button>
        </>}>
        <form id="create-guest-form" onSubmit={handleSubmit(onCreateGuest)} className="grid grid-cols-2 gap-4">
          {[
            ['firstName','First Name','text',true],
            ['lastName','Last Name','text',true],
            ['email','Email','email',true],
            ['phone','Phone','tel',false],
            ['nationality','Nationality','text',false],
            ['city','City','text',false],
            ['country','Country','text',false],
          ].map(([name, label, type, required]) => (
            <div key={name}>
              <label className="form-label">{label}</label>
              <input {...register(name, required ? { required: `${label} is required` } : {})}
                type={type} className="form-input" />
              {errors[name] && <p className="form-error">{errors[name].message}</p>}
            </div>
          ))}
        </form>
      </Modal>

      {/* Detail Modal */}
      <Modal isOpen={!!detail} onClose={() => setDetail(null)} title="Guest Profile" size="lg">
        {detail && (
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div><p className="text-gray-500">Full Name</p><p className="font-medium">{detail.firstName} {detail.lastName}</p></div>
            <div><p className="text-gray-500">Email</p><p>{detail.email}</p></div>
            <div><p className="text-gray-500">Phone</p><p>{detail.phone ?? '—'}</p></div>
            <div><p className="text-gray-500">Nationality</p><p>{detail.nationality ?? '—'}</p></div>
            <div><p className="text-gray-500">Loyalty Tier</p><Badge status={detail.loyaltyTier} /></div>
            <div><p className="text-gray-500">Loyalty Points</p><p className="font-semibold text-yellow-600">{detail.loyaltyPoints?.toLocaleString()}</p></div>
            <div><p className="text-gray-500">ID Type</p><p>{detail.idType ?? '—'}</p></div>
            <div><p className="text-gray-500">ID Number</p><p className="font-mono">{detail.idNumber ?? '—'}</p></div>
          </div>
        )}
      </Modal>
    </div>
  )
}
