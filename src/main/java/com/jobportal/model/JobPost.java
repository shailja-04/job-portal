package com.jobportal.model;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class JobPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int postId;

	@NotBlank(message = "Job profile is required")
	private String postProfile;

	@NotBlank(message = "Job description is required")
	private String postDesc;

	@NotNull(message = "Required experience is required")
	@Min(value = 0, message = "Experience cannot be negative")
	private Integer reqExperience;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> postTechStack;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "posted_by_id")
	private AppUser postedBy;

	private LocalDateTime createdAt = LocalDateTime.now();

	private LocalDateTime updatedAt = LocalDateTime.now();

	public JobPost(int postId, String postProfile, String postDesc, Integer reqExperience, List<String> postTechStack) {
		this.postId = postId;
		this.postProfile = postProfile;
		this.postDesc = postDesc;
		this.reqExperience = reqExperience;
		this.postTechStack = postTechStack;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

}
