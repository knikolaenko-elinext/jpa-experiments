package com.knick.exp.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
	@Id
	@GeneratedValue
	private Long id;
	private String text;
	private int counter;

	@OneToOne(mappedBy = "message", fetch = FetchType.LAZY)
	private MessageDeliveryReport deliveryReport;

	public Message(Long id, String text, int counter) {
		super();
		this.id = id;
		this.text = text;
		this.counter = counter;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", text=" + text + ", counter=" + counter + ", deliveryReport=" + deliveryReport + "]";
	}
}
