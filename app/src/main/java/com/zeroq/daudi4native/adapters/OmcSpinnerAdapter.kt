package com.zeroq.daudi4native.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.data.models.OmcModel


class OmcSpinnerAdapter(var ctx: Context, var resource: Int, var omcs: ArrayList<OmcModel>) :
    ArrayAdapter<OmcModel>(ctx, resource, omcs) {

    var inflater: LayoutInflater =
        ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent);
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = inflater.inflate(resource, parent, false)

        val omc: OmcModel = omcs[position]
        val label: TextView = row.findViewById(R.id.spinnerOmc)

        label.text = omc.name


        return row
    }

}