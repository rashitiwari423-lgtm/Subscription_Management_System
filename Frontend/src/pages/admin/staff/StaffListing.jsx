import React, { useState } from 'react';
import { Search, Plus, UserCheck, Shield, Mail, Phone, MoreHorizontal } from 'lucide-react';
import { staffMembers as initialStaff } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function StaffListing() {
  const [staffList, setStaffList] = useState(initialStaff || []);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Staff Operations & RBAC Directory</h1>
          <p className="text-xs text-on-surface-variant">
            Manage operational teams, customer assignments, and enterprise security permissions.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/staff/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add Staff Member</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {staffList.map((member) => (
          <div key={member.id} className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3">
            <div className="flex items-start justify-between">
              <div className="w-10 h-10 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center text-sm">
                {member.name.split(' ').map(n => n[0]).join('')}
              </div>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                {member.status}
              </span>
            </div>

            <div>
              <h3 className="font-bold text-sm text-on-surface">{member.name}</h3>
              <p className="text-xs text-primary font-medium">{member.role}</p>
              <p className="text-[11px] text-on-surface-variant font-mono mt-0.5">{member.email}</p>
            </div>

            <div className="pt-3 border-t border-surface-container text-xs flex justify-between items-center text-on-surface-variant">
              <span>Assigned Accounts:</span>
              <span className="font-bold text-on-surface font-mono">{member.assignedCustomers} orgs</span>
            </div>

            <div className="pt-2 flex gap-2">
              <button
                onClick={() => navigate(`/admin/staff/${member.id}`)}
                className="w-full py-1.5 border border-outline-variant/40 hover:bg-surface-container rounded-lg text-xs font-semibold text-on-surface"
              >
                Inspect Portfolio
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

