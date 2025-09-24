package com.modelbest.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 技术点列表适配器
 */
class TechPointAdapter(
    private var techPoints: List<TechPoint>,
    private val onItemClick: (TechPoint) -> Unit
) : RecyclerView.Adapter<TechPointAdapter.TechPointViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechPointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tech_point, parent, false)
        return TechPointViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechPointViewHolder, position: Int) {
        holder.bind(techPoints[position])
    }

    override fun getItemCount(): Int = techPoints.size

    fun updateData(newTechPoints: List<TechPoint>) {
        techPoints = newTechPoints
        notifyDataSetChanged()
    }

    inner class TechPointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIndicator: View = itemView.findViewById(R.id.view_category_indicator)
        private val titleText: TextView = itemView.findViewById(R.id.tv_title)
        private val categoryText: TextView = itemView.findViewById(R.id.tv_category)
        private val difficultyText: TextView = itemView.findViewById(R.id.tv_difficulty)
        private val descriptionText: TextView = itemView.findViewById(R.id.tv_description)
        private val tagsRecyclerView: RecyclerView = itemView.findViewById(R.id.rv_tags)

        init {
            // 设置标签RecyclerView
            tagsRecyclerView.layoutManager = LinearLayoutManager(
                itemView.context, 
                LinearLayoutManager.HORIZONTAL, 
                false
            )
            
            // 设置点击监听
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(techPoints[position])
                }
            }
        }

        fun bind(techPoint: TechPoint) {
            // 设置基本信息
            titleText.text = techPoint.title
            categoryText.text = techPoint.category.displayName
            descriptionText.text = techPoint.description
            difficultyText.text = techPoint.difficulty.displayName

            // 设置分类指示器颜色
            val categoryColor = ContextCompat.getColor(itemView.context, techPoint.category.colorRes)
            categoryIndicator.setBackgroundColor(categoryColor)

            // 设置难度标签颜色
            val difficultyColor = when (techPoint.difficulty) {
                Difficulty.BEGINNER -> ContextCompat.getColor(itemView.context, R.color.teal_200)
                Difficulty.INTERMEDIATE -> ContextCompat.getColor(itemView.context, R.color.purple_500)
                Difficulty.ADVANCED -> ContextCompat.getColor(itemView.context, R.color.teal_700)
                Difficulty.EXPERT -> ContextCompat.getColor(itemView.context, R.color.colorError)
            }
            difficultyText.setBackgroundColor(difficultyColor)

            // 设置标签
            val tagAdapter = TagAdapter(techPoint.tags)
            tagsRecyclerView.adapter = tagAdapter
        }
    }
}

/**
 * 标签适配器
 */
class TagAdapter(private val tags: List<String>) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount(): Int = tags.size

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagText: TextView = itemView as TextView

        fun bind(tag: String) {
            tagText.text = tag
        }
    }
}
