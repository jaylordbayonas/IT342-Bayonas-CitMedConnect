import React from 'react';

export const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  className = '',
  onClick,
  icon: Icon,
  iconPosition = 'left',
  ...props
}) => {
  const classes = [
    'btn',
    `btn-${variant}`,
    `btn-${size}`,
    className,
  ].filter(Boolean).join(' ');

  return (
    <button type="button" className={classes} onClick={onClick} {...props}>
      {Icon && iconPosition === 'left' && <Icon size={16} />}
      <span>{children}</span>
      {Icon && iconPosition === 'right' && <Icon size={16} />}
    </button>
  );
};

export const Card = ({ children, className = '', hover = false, ...props }) => {
  const classes = ['card', hover ? 'card-hover' : '', className].filter(Boolean).join(' ');
  return (
    <div className={classes} {...props}>
      {children}
    </div>
  );
};
