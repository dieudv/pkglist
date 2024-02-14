package com.pkglist.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkglist.R
import com.pkglist.adapter.PkgAdapter
import com.pkglist.adapter.PkgAdapter.ItemClickListener
import com.pkglist.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), MenuProvider, ItemClickListener, PkgAdapter.ItemRemoved {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var adapter: PkgAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = PkgAdapter(requireContext(), getPackages(APP_USER_INSTALLED))
        adapter.setClickListener(this)
        binding.recyclerview.adapter = adapter
    }

    override fun onItemClick(view: View?, position: Int) {
        val popup = PopupMenu(requireContext(), view, Gravity.END)
        popup.menuInflater.inflate(R.menu.app_menu, popup.menu)
        popup.setOnMenuItemClickListener { v ->
            when (v.itemId) {
                R.id.copy_package_name -> {
                    requireContext().copyToClipboard(adapter.getItem(position).packageName)
                }

                R.id.launch -> {
                    val launchIntent = requireActivity().packageManager.getLaunchIntentForPackage(
                        adapter.getItem(position).packageName
                    )
                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    } else {
                        Toast.makeText(requireContext(), "Can't open this app!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                R.id.app_info -> {
                    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    i.addCategory(Intent.CATEGORY_DEFAULT)
                    i.data = Uri.parse("package:" + adapter.getItem(position).packageName)
                    startActivity(i)
                }
            }
            true
        }
        popup.show()
    }

    override fun onItemRemoved(position: Int) {
        adapter.packages.removeAt(position)
        adapter.notifyItemRemoved(position)
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_all -> {
                adapter = PkgAdapter(requireContext(), getPackages(APP_ALL))
            }

            R.id.action_system -> {
                adapter = PkgAdapter(requireContext(), getPackages(APP_SYSTEM))
            }

            R.id.action_pre_installed -> {
                adapter = PkgAdapter(requireContext(), getPackages(APP_PRE_INSTALLED))
            }

            R.id.action_user_installed -> {
                adapter = PkgAdapter(requireContext(), getPackages(APP_USER_INSTALLED))
            }
        }

        adapter.setClickListener(this)
        binding.recyclerview.adapter = adapter
        return true
    }

    private fun getPackages(flag: Int): ArrayList<ApplicationInfo> {
        val pm: PackageManager = requireActivity().packageManager
        val allPackages = pm.getInstalledApplications(0) as ArrayList<ApplicationInfo>
        val packages = ArrayList<ApplicationInfo>()

        when (flag) {
            APP_SYSTEM -> {
                for (pkg in allPackages) {
                    val launchIntent =
                        requireActivity().packageManager.getLaunchIntentForPackage(pkg.packageName)
                    if (launchIntent == null) {
                        packages.add(pkg)
                    }
                }
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.action_system_apps)
                return packages
            }

            APP_PRE_INSTALLED -> {
                for (pkg in allPackages) {
                    val launchIntent =
                        requireActivity().packageManager.getLaunchIntentForPackage(pkg.packageName)
                    if (launchIntent != null && pkg.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        packages.add(pkg)
                    }
                }
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.action_pre_installed)
                return packages
            }

            APP_USER_INSTALLED -> {
                for (pkg in allPackages) {
                    val launchIntent =
                        requireActivity().packageManager.getLaunchIntentForPackage(pkg.packageName)
                    if (launchIntent != null && pkg.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                        packages.add(pkg)
                    }
                }
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.action_user_installed)
                return packages
            }

            else -> {
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.action_all_apps)
                return allPackages
            }
        }
    }

    private fun Context.copyToClipboard(text: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("pkg_name", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val APP_ALL = 0
        private const val APP_SYSTEM = 1
        private const val APP_PRE_INSTALLED = 2
        private const val APP_USER_INSTALLED = 3
    }
}